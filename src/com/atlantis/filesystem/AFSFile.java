/* File: File.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 16 Jul 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 *  Base class for accessing and dealing with AFS files.
 */


package com.atlantis.filesystem;

import com.atlantis.utilities.Conversions;
import com.hardware.ManagedHardDisk;

import java.util.ArrayList;

public class AFSFile {
    //Instance variables
    protected AFS_FILE_TYPE type;
    private long start_block;
    private long size;
    private String name;
    private ManagedHardDisk disk;
    protected long file_pointer;

    /* Precond:
     *  disk is a valid ManagedHardDisk where the file lives.
     *  name is a String which represents the file's name (not full path).
     *  start_block is the starting block where the file lives on the disk.
     *
     * Postcond:
     *  Loads in the file info and allows for dealing with the file.
     */
    public AFSFile(ManagedHardDisk disk, String name, long start_block){
        this.disk = disk;
        this.name = name;
        this.start_block = start_block;
        this.type = AFS_FILE_TYPE.DATA;
        this.size = 0;
        // Compute size of file
        long current_block = this.start_block;
        do{
            this.size += Conversions.bytes_to_int(disk.read_bytes(4, current_block, 8));
            current_block = Conversions.bytes_to_long(disk.read_bytes(8, current_block, 1));
        }while(current_block != -1);
        this.file_pointer = 0;
    }

    /* Precond:
     *  file is a valid AFSFile object to copy.
     *
     * Postcond:
     *  Creates a copy of the file (used mainly for changing class).
     */
    protected AFSFile(AFSFile file){
        this.disk = file.disk;
        this.name = file.name;
        this.start_block = file.start_block;
        this.type = file.type;
        this.size = file.size;
        this.file_pointer = file.file_pointer;
    }

    //======================
    //  Accessor Methods
    //======================

    public long get_size(){
        return size;
    }

    public String get_name(){
        return name;
    }

    public AFS_FILE_TYPE get_type(){
        return type;
    }

    public long get_start(){
        return start_block;
    }

    /* Precond:
     *  fp is the new value to set the file pointer to.
     *
     * Postcond:
     *  Sets the file pointer to the new value.
     *  Returns true if the file pointer is still valid.
     */
    public boolean seek(long fp){
        file_pointer = fp;
        return file_pointer >= 0 && file_pointer < size;
    }

    /* Precond:
     *  incr is the amount of bytes to move the file pointer forward.
     *
     * Postcond:
     *  Increments the file pointer by the given value.
     *  Returns true if the file pointer is still valid.
     */
    public boolean advance(long incr){
        file_pointer += incr;
        return file_pointer >= 0 && file_pointer < size;
    }

    /* Precond:
     *  None.
     *
     * Postcond:
     *  Resets the file pointer to the beginning of the file.
     */
    public void reset(){
        file_pointer = 0;
    }

    /* Precond:
     *  None.
     *
     * Postcond:
     *  Sets the file pointer to the end of the file.
     */
    public void end(){
        file_pointer = size;
    }

    /* Precond:
     *  None.
     *
     * Postcond:
     *  Returns true if the file pointer is at the end of the file.
     */
    public boolean eof(){
        return file_pointer == size;
    }

    /* Precond:
     *  length is an integer representing the number of bytes to read.
     *
     * Postcond:
     *  Returns a byte array containing length bytes from the file starting at the file pointer.
     *  Returns null if the read goes off the buffer.
     *  Forwards the file pointer by length (if read is successful)
     */
    public byte[] read(int length){
        long fp = this.file_pointer;
        byte[] result = new byte[length];
        long block, offset;
        int idx = 0;
        while(length > 0){
            block = block_address(fp);
            offset = block_offset(fp);
            if(block == -1)return null;
            int read_length = (int)(disk.get_block_size()-offset);
            read_length = Math.max(read_length, length);
            byte[] data = disk.read_bytes(read_length, block, offset);
            for(byte datum : data){
                result[idx++] = datum;
            }
            fp += read_length;
            length -= read_length;
        }
        file_pointer = fp;
        return result;
    }

    /* Precond:
     *
     * Postcond:
     *  Returns a byte from the file starting at the file pointer.
     *  Returns null if the read goes off the buffer.
     *  Forwards the file pointer by length (if read is successful)
     */
    public byte read_byte(){
        long block = block_address(file_pointer);
        if(block == -1)return -1;
        long offset = block_offset(file_pointer);
        file_pointer += 1;
        return disk.read_byte(block, offset);
    }

    /* Precond:
     *  data is an array of bytes to write to the file on disk.
     *
     * Postcond:
     *  Writes the data, extending the file if needed.
     *  Returns false if write failed for any reason.
     */
    public boolean write(byte[] data){
        for(byte datum : data){
            if(!write_byte(datum))return false;
        }
        return true;
    }

    /* Precond:
     *  data is a byte to write to the file on disk.
     *
     * Postcond:
     *  Writes the data, extending the file if needed.
     *  Returns false if write failed for any reason.
     */
    public boolean write_byte(byte data){
        //Allocate new blocks as needed.
        if(block_address(file_pointer) == -1){
            long last_block = start_block;
            long next_block = start_block;
            do{
                last_block = next_block;
                next_block = Conversions.bytes_to_long(disk.read_bytes(8, last_block, 0));
            }while(next_block != -1);

            //Allocate new block
            long free_block = disk.next_free_block();
            if(free_block == -1){
                return false;
            }
            byte[] next_free_block = Conversions.long_to_bytes(free_block);
            disk.allocate_block(free_block);
            disk.write_bytes(next_free_block, last_block, 1);
        }
        long block = block_address(file_pointer);
        long offset = block_offset(file_pointer);
        disk.write_byte(data, block, offset);
        file_pointer += 1;
        size += 1;
        return true;
    }

    //=====================
    //  PRIVATE METHODS
    //=====================

    /* Precond:
     *  fp is the file pointer to calculate the block for.
     *
     * Postcond:
     *  Returns the disk block address for the file pointer.
     *  Returns -1 if the address is unreachable.
     */
    private long block_address(long fp){
        long current_block = this.start_block;
        long block_size = disk.get_block_size() - FileManager.file_block_preamble_length;
        while(fp >= block_size && current_block != -1){
            fp -= block_size;
            current_block = Conversions.bytes_to_long(disk.read_bytes(8, current_block, 0));
        }
        return current_block;
    }

    /* Precond:
     *  fp is the file pointer to calculate the offset for
     *
     * Postcond:
     *  Returns the disk block offset address for the current file pointer.
     */
    private long block_offset(long fp){
        return fp % (disk.get_block_size() - FileManager.file_block_preamble_length);
    }
}
