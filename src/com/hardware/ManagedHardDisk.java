/* File: ManagedHardDisk.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 12 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 *  -1 indicates an unused byte.
 *  File structure:
 *      byte 0) 0 or 1 to indicate if the space is in use.
 *      byte 1-4) Length (in bytes) of the file.
 *      Next Block) Null terminated String.
 *      Next Block) File data.
 */


package com.hardware;

import java.util.ArrayList;
import java.util.Arrays;

public class ManagedHardDisk{
    //Constants
    private static final byte[] magic = {11, 13, 27};
    private static final int byte_mask = 0xFFFF;


    //Singleton Variables
    private static ManagedHardDisk instance = null;

    //Instance variables
    private HardDisk disk;
    private final long raw_disk;
    private ArrayList<SAC_File> files;


    //=========================
    //  Singleton Methods
    //=========================

    public static ManagedHardDisk getInstance(){
        if(instance == null)instance = new ManagedHardDisk();
        return instance;
    }

    public static ManagedHardDisk getInstance(String diskname){
        if(instance == null)instance = new ManagedHardDisk(diskname);
        else if(!instance.disk.getFilename().equals(diskname)){
            instance.close();
            instance = new ManagedHardDisk(diskname);
        }
        return instance;
    }


    //=========================
    //  Instance Methods
    //=========================

    //Constructor
    private ManagedHardDisk(){
        disk = HardDisk.getInstance();
        if(!isFormatted())format();
        raw_disk = disk.getSize()/2;
        files = new ArrayList<>();
        populate();
    }

    private ManagedHardDisk(String diskname){
        disk = HardDisk.getInstance(diskname);
        if(!isFormatted())format();
        raw_disk = disk.getSize()/2;
        files = new ArrayList<>();
        populate();
    }

    //===========================
    //  Public Methods
    //===========================

    //Precond:
    //  filename is the name of the file to delete.
    //
    //Postcond:
    //  Removes (but does not wipe, the file with the given name.
    public void delete_file(String filename){
        for(int i = 0;i < files.size();i++){
            if(files.get(i).name.equals(filename)){
                erase_file(files.get(i));
                files.remove(i);
                return;
            }
        }
    }

    public void close(){
        for(SAC_File f : files){
            f.write(disk);
        }
        disk.close();
        disk = null;
        files = null;
    }

    //===========================
    //  Private Methods
    //===========================
    private boolean isFormatted(){
        long fp = disk.getFilePointer();
        disk.seek(0);
        byte[] magic = disk.read(3);
        disk.seek(fp);
        return Arrays.equals(magic, ManagedHardDisk.magic);
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Populates the file table for the disk.
    private void populate(){
        disk.seek(3);
        while(disk.getFilePointer() < raw_disk){
            long start = disk.getFilePointer();
            byte valid = disk.readByte();
            if(valid == 1) {
                int size = disk.readInt();
                String name = disk.readString();
                files.add(new SAC_File(name, start, size));
            }
            else seek_next();
        }
    }

    //Precond:
    //  f is a valid SAC_File.
    //
    //Postcond:
    //  Erases the file from the disk.
    private void erase_file(SAC_File f){
        disk.seek(f.start);
        disk.write((byte)0);
    }

    //Precond:
    //  None.
    //Postcond:
    //  Defragments the hard disk.
    private void defrag(){
        for(SAC_File f : files){
            f.read_file(disk);
        }
        format();
        long start = 3L;
        for(SAC_File f : files){
            f.start = start;
            f.write(disk);
            start = disk.getFilePointer();
            f.close(disk);
        }
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Formats the disk to empty (-1 for all bytes.)
    private void format(){
        byte[] blank_kb = new byte[(int)HardDisk.KB];
        Arrays.fill(blank_kb, (byte)-1);
        disk.seek(0);
        for(long i = 0;i < disk.getSize()/HardDisk.KB;i++){
            disk.write(blank_kb);
        }
        disk.seek(0);
        disk.write(magic);
        disk.seek(0);
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Seeks the next non-blank space in memory.
    private void seek_next(){
        while(disk.read(1)[0] == -1 && disk.getFilePointer() < disk.getSize());
        disk.seek(disk.getFilePointer()-1);
    }

    //==============================
    //  Private Classes
    //==============================
    private static class SAC_File{
        private final String name;
        public long start;
        private final int length;
        private byte[] data;
        private boolean changed;

        public SAC_File(String name, long start, int length){
            this.name = name;
            this.start = start;
            this.length = length;
            data = null;
            changed = false;
        }

        public byte[] read_file(HardDisk disk){
            if(data != null)return data;
            long data_start = start + Integer.BYTES + (2L *name.length()) + 2L;
            disk.seek(data_start);
            data = disk.read(length);
            return data;
        }

        public boolean change(byte[] data, int offset){
            if(!can_modify(offset, data.length))return false;
            System.arraycopy(data, 0, this.data, offset, data.length);
            changed = true;
            return true;
        }

        public void write(HardDisk disk){
            if(data == null)return;
            byte[] byte_name = name.getBytes();
            disk.seek(start);
            disk.write((byte)1);
            disk.write(length);
            disk.write(byte_name);
            disk.write((byte)0);
            disk.write(data);
        }

        public void close(HardDisk disk){
            if(changed)write(disk);
            data = null;
        }

        private boolean can_modify(int offset, int length){
            return offset+length < this.length;
        }

    }
}
