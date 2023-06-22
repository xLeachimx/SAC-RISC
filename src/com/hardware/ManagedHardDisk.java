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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.RandomAccess;

public class ManagedHardDisk {
    //Constants
    private static final byte[] magic = {11, 13, 27};
    private static final int byte_mask = 0xFFFF;
    private static final long KB = 1024L;
    private static final long MB = KB*KB;

    //Instance defaults
    private static final long default_disk_size = 10*MB;
    private static final long default_block_size = KB;
    private static final String default_disk_name = "SAC.dsk";

    //Singleton Variables
    public static ManagedHardDisk instance = null;

    //Singleton Methods
    //Creation Methods
    public static ManagedHardDisk create(){
        return create(default_disk_name, default_disk_size);
    }
    public static ManagedHardDisk create(String filename, long disk_size){
        if(instance != null)unmount();
        instance = new ManagedHardDisk(filename, disk_size);
        return instance;
    }

    public static ManagedHardDisk mount(String filename){
        if(instance != null)unmount();
        instance = new ManagedHardDisk(filename);
        return instance;
    }

    public static ManagedHardDisk get_instance(){
        if(instance == null)throw new NullPointerException();
        return instance;
    }

    public static void unmount(){
        if(instance != null)instance.close();
        instance = null;
    }

    //Instance Variables
    private int blocks;
    private long disk_size;
    private final String filename;
    private final File handle;
    private RandomAccessFile access;
    private static final long preamble_length = magic.length + Integer.BYTES; //How long the disk preamble is.


    //Instance Methods

    //Precond:
    //  filename is the name of the file where the data is to be stored.
    //  disk_size is the size to make the disk.
    //
    //Postcond:
    //  Creates a new ManagedHardDisk object with data stored in the given filename.
    //  Creates a new hard disk file, of the specified size, that has been formatted and cleared.
    private ManagedHardDisk(String filename, long disk_size){
        this.filename = filename;
        this.disk_size = disk_size;
        handle = new File(filename);
        blocks = (int)(disk_size/default_block_size);
        if(disk_size % default_block_size != 0)blocks += 1;
        this.disk_size = (blocks*(default_block_size+1)) + preamble_length;
        access = null;
        format();
        close();
    }

    //Precond:
    //  filename is the name of the file where the data is stored.
    //
    //Postcond:
    //  Creates a new ManagedHardDisk object with data stored in the given filename.
    //  Opens an existing hard disk file.
    private ManagedHardDisk(String filename){
        this.filename = filename;
        handle = new File(filename);
        access = null;
        open();
        try {
            disk_size = access.length();
            access.seek(0);
            for(int i = 0;i < magic.length; i++){
                if(magic[i] != access.readByte()){
                    System.err.println("Problem opening existing hard disk in " + filename);
                    close();
                    System.exit(205);
                }
            }
            blocks = access.readInt();
        } catch (IOException exp) {
            System.err.println("Problem opening existing hard disk in " + filename);
            exp.printStackTrace();
            close();
            System.exit(205);
        }
        close();
    }

    //======================
    //  File Access Methods
    //======================

    //Precond:
    //  None.
    //
    //Postcond:
    //  Opens the hard disk file so it can be read from and written to.
    public void open(){
        if(access != null)return;
        try {
            access = new RandomAccessFile(handle, "rw");
        } catch (IOException exp){
            System.err.println("Problem opening hard disk.");
            exp.printStackTrace();
            System.exit(200);
        }
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Closes file access, if the file is open.
    public void close(){
        if(access == null)return;
        try {
            access.close();
        } catch (IOException exp){
            System.err.println("Problem closing hard disk.");
            exp.printStackTrace();
            System.exit(200);
        }
    }

    //=========================
    //  Hard Disk Read/Write
    //=========================

    //Precond:
    //  bytes is an array of byte values to be written to the disk.
    //  block is the block to write the bytes to.
    //  offset is how far to offset the writing location from the beginning of the block.
    //
    //Postcond:
    //  Writes the bytes at the specified location.
    //  Sets the block flag to in-use.
    //  WARNING: This does not prevent writing past block boundary.
    public void write_bytes(byte[] bytes, long block, long offset){
        if(access == null){
            System.err.println("Attempted to write to a closed hard disk.");
            System.exit(201);
        }
        try{
            long data_addr = compute_address(block, offset);
            long block_flag = compute_address(block, -1);
            access.seek(block_flag);
            access.writeByte(1);
            access.seek(data_addr);
            access.write(bytes);
        } catch (IOException exp){
            System.err.println("Problem writing bytes to disk.");
            exp.printStackTrace();
            System.exit(201);
        }
    }

    //Precond:
    //  val is a byte value to be written to the disk.
    //  block is the block to write the bytes to.
    //  offset is how far to offset the writing location from the beginning of the block.
    //
    //Postcond:
    //  Writes the byte at the specified location.
    //  Sets the block flag to in-use.
    //  WARNING: This does not prevent writing past block boundary.
    public void write_byte(byte val, long block, long offset){
        if(access == null){
            System.err.println("Attempted to write to a closed hard disk.");
            System.exit(201);
        }
        try{
            long data_addr = compute_address(block, offset);
            long block_flag = compute_address(block, -1);
            access.seek(block_flag);
            access.writeByte(1);
            access.seek(data_addr);
            access.writeByte(val);
        } catch (IOException exp){
            System.err.println("Problem writing bytes to disk.");
            exp.printStackTrace();
            System.exit(201);
        }
    }

    //Precond:
    //  length is the number of bytes to read from the disk.
    //  block is the block to write the bytes to.
    //  offset is how far to offset the writing location from the beginning of the block.
    //
    //Postcond:
    //  Reads the bytes at the specified location.
    //  WARNING: This does not prevent reading past block boundary.
    public byte[] read_bytes(int length, long block, long offset){
        if(access == null){
            System.err.println("Attempted to read from a closed hard disk.");
            System.exit(202);
        }
        byte[] buffer = new byte[length];
        try{
            long data_addr = compute_address(block, offset);
            access.seek(data_addr);
            access.read(buffer);
        } catch (IOException exp){
            System.err.println("Problem reading bytes from disk.");
            exp.printStackTrace();
            System.exit(202);
        }
        return buffer;
    }

    //Precond:
    //  block is the block to write the bytes to.
    //  offset is how far to offset the writing location from the beginning of the block.
    //
    //Postcond:
    //  Reads the bytes at the specified location.
    //  WARNING: This does not prevent reading past block boundary.
    public byte read_byte(long block, long offset){
        if(access == null){
            System.err.println("Attempted to read from a closed hard disk.");
            System.exit(202);
        }
        try{
            long data_addr = compute_address(block, offset);
            access.seek(data_addr);
            return access.readByte();
        } catch (IOException exp){
            System.err.println("Problem reading bytes from disk.");
            exp.printStackTrace();
            System.exit(202);
        }
        return 0;
    }

    //========================
    //  Hard Disk Management
    //=-======================

    //Precond:
    //  None.
    //
    //Postcond:
    //  Performs a quick format of the hard disk.
    public void format(){
        open();
        try{
            access.setLength(disk_size);
            access.seek(0);
            access.write(magic);
            access.writeInt(blocks);
        } catch(IOException exp){
            System.err.println("Problem formatting hard disk.");
            exp.printStackTrace();
            System.exit(203);
        }
        for(long block = 0; block < blocks;block++){
            free_block(block);
        }
        close();
    }

    //Precond:
    //  block is the block to free.
    //
    //Postcond:
    //  Changes the block's flag to free.
    public void free_block(long block){
        if(access == null){
            System.err.println("Attempted to deleting block data in closed hard disk.");
            System.exit(204);
        }
        try{
            long block_flag = compute_address(block, -1);
            access.seek(block_flag);
            access.writeByte(0);
        } catch (IOException exp){
            System.err.println("Problem deleting block data from disk.");
            exp.printStackTrace();
            System.exit(204);
        }
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Returns the next free block.
    //  Returns -1 if there is no free block remaining.
    public long next_free_block(){
        if(access == null){
            System.err.println("Attempted to search closed hard disk.");
            System.exit(206);
        }
        try{
            for(long block = 0; block < blocks;block++) {
                long block_flag = compute_address(block, -1);
                access.seek(block_flag);
                if(access.readByte() == 0)return block;
            }
        } catch (IOException exp){
            System.err.println("Problem checking block data from disk.");
            exp.printStackTrace();
            System.exit(204);
        }
        return -1;
    }

    //====================
    //  Private Methods
    //====================

    //Precond:
    //  block is the block of the address.
    //  offset is the byte offset of the address.
    //
    //Postcond:
    //  Returns the byte address block and offset.
    private long compute_address(long block, long offset){
        return (((default_block_size+1) * block) + offset) + preamble_length + 1;
    }
}
