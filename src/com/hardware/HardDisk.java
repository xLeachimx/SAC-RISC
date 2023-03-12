/* File: HardDisk.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 15 Feb 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class to act as a Hard Disk for the SAC-RISC VM.
 * Notes:
 *  All data is stored on disk in a series of random access files.
 *  Multiple hard drives can be made.
 *  Default size is 20MB.
 *  Minimum size is 10MB.
 *  First three bytes of an already set up HardDisk file are:
 *      0) 0x2A
 *      1) 0x1B
 *      2) 0xAD
 *  Each file in the partition table takes up 92 bytes.
 *  The maximum number of possible files is 200.
 *  Filenames are only allowed to be <40 characters long (names are truncated if too long.)
 *  Files are comprised of blocks, each block is 1KB in size.
 *  File may be up to 3 blocks in size.
 *  Block addresses are 4 bytes in length.
 */


package com.hardware;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class HardDisk {
    //Basic Disk Constants
    public static final long KB = 1024;
    public static final long MB = 1024*KB;
    private static final long DISK_SIZE = 10*MB;

    //Singleton variables
    private static HardDisk instance = null;

    //Instance variables
    private final String filename;
    private final File handle;
    private RandomAccessFile fio;

    //==============================
    //  Singleton Methods
    //==============================
    public static HardDisk getInstance(String diskname){
        if(instance == null){
            instance = new HardDisk(diskname);
        }
        else if(!instance.filename.equals(diskname)){
            instance.close();
            instance = new HardDisk(diskname);
        }
        return instance;
    }

    public static HardDisk getInstance(){
        if(instance == null){
            instance = new HardDisk("SAC-HD");
        }
        return instance;
    }


    //==============================
    //  Instance Methods
    //==============================

    //Constructor
    private HardDisk(String filename){
        this.filename = filename;
        handle = new File(filename);
        try {
            fio = new RandomAccessFile(handle, "rw");
            fio.setLength(DISK_SIZE);
        } catch(IOException exp){
            System.out.println("Problem accessing hard disk file.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_ACCESS_ERROR.ordinal());
        }
    }

    //=============================
    //  Public Methods
    //=============================

    //Getters
    public String getFilename(){
        return filename;
    }

    public boolean isOpen(){
        return fio != null;
    }

    public long getFilePointer(){
        if(fio == null)return -1;
        try {
            return fio.getFilePointer();
        } catch(IOException exp){
            System.out.println("Problem retrieving hard disk information.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_RETRIEVAL_ERROR.ordinal());
        }
        return -1;
    }

    public long getSize(){
        if(fio == null)return 0;
        try{
            return fio.length();
        } catch(IOException exp){
            System.out.println("Problem retrieving hard disk information.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_RETRIEVAL_ERROR.ordinal());
        }
        return 0;
    }


    //Precond:
    //  None.
    //
    //Postcond:
    //  Opens the hard disk file contained by handle.
    public void open(){
        if(fio != null)return;
        try {
            fio = new RandomAccessFile(handle, "rw");
            fio.setLength(DISK_SIZE);
        } catch(IOException exp){
            System.out.println("Problem accessing hard disk file.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_ACCESS_ERROR.ordinal());
        }
    }

    //Precond:
    //  position is the byte to seek to.
    //
    //Postcond:
    //  Seeks the random access file to the given position.
    public boolean seek(long position){
        if(fio == null)return false;
        try {
            fio.seek(position);
        } catch(IOException exp){
            System.out.println("Problem seeking hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_SEEK_ERROR.ordinal());
        }
        return true;
    }

    //Precond:
    //  length is the number of bytes to read.
    //
    //Postcond:
    //  Returns a byte array retrieved from the hard disk file.
    //  Return null if the file is closed.
    public byte[] read(int length){
        if(fio == null)return null;
        byte[] result = new byte[length];
        try {
            fio.read(result);
        } catch(IOException exp){
            System.out.println("Problem reading from hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_READ_ERROR.ordinal());
        }
        return result;
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Returns an integer retrieved from the hard disk file.
    //  Return null if the file is closed.
    public Integer readInt(){
        if(fio == null)return null;
        Integer result = null;
        try {
            result = fio.readInt();
        } catch(IOException exp){
            System.out.println("Problem reading from hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_READ_ERROR.ordinal());
        }
        return result;
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Returns a byte retrieved from the hard disk file.
    //  Return null if the file is closed.
    public Byte readByte(){
        if(fio == null)return null;
        Byte result = null;
        try {
            result = fio.readByte();
        } catch(IOException exp){
            System.out.println("Problem reading from hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_READ_ERROR.ordinal());
        }
        return result;
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Returns a null terminated String retrieved from the hard disk file.
    //  Return null if the file is closed.
    public String readString(){
        if(fio == null)return null;
        StringBuilder result = new StringBuilder();
        try {
            char temp;
            do{
                temp = fio.readChar();
                if(temp != 0)result.append(temp);
            }while(temp != 0);
        } catch(IOException exp){
            System.out.println("Problem reading from hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_READ_ERROR.ordinal());
        }
        return result.toString();
    }

    //Precond:
    //  data is a byte array of data to be written to the file at the current file pointer.
    //
    //Postcond:
    //  Returns true if the array's contents are written to the hard disk file.
    //  Returns false if the file is closed.
    public boolean write(byte[] data){
        if(fio == null)return false;
        try {
            fio.write(data);
        } catch(IOException exp){
            System.out.println("Problem writing to hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_WRITE_ERROR.ordinal());
        }
        return true;
    }

    //Precond:
    //  data is a byte of data to be written to the file at the current file pointer.
    //
    //Postcond:
    //  Returns true if the byte's contents are written to the hard disk file.
    //  Returns false if the file is closed.
    public boolean write(byte data){
        if(fio == null)return false;
        try {
            fio.writeByte(data);
        } catch(IOException exp){
            System.out.println("Problem writing to hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_WRITE_ERROR.ordinal());
        }
        return true;
    }

    //Precond:
    //  data is an integer to be written to the file at the current file pointer.
    //
    //Postcond:
    //  Returns true if the array's contents are written to the hard disk file.
    //  Returns false if the file is closed.
    public boolean write(int data){
        if(fio == null)return false;
        try {
            fio.writeInt(data);
        } catch(IOException exp){
            System.out.println("Problem writing to hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_WRITE_ERROR.ordinal());
        }
        return true;
    }

    //Precond:
    //  data is a long integer to be written to the file at the current file pointer.
    //
    //Postcond:
    //  Returns true if the array's contents are written to the hard disk file.
    //  Returns false if the file is closed.
    public boolean write(long data){
        if(fio == null)return false;
        try {
            fio.writeLong(data);
        } catch(IOException exp){
            System.out.println("Problem writing to hard disk.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_WRITE_ERROR.ordinal());
        }
        return true;
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Closes the open file.
    public void close(){
        if(fio == null)return;
        try {
            fio.close();
        } catch(IOException exp){
            System.out.println("Problem closing hard dist file.");
            exp.printStackTrace();
            System.exit(Interupts.HARDDISK_CLOSE_ERROR.ordinal());
        }
        fio = null;
    }
}
