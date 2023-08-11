/* File: Directory.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 16 Jul 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.atlantis.filesystem;

import com.atlantis.utilities.Conversions;
import com.hardware.ManagedHardDisk;

import java.util.ArrayList;

public class AFSDirectory extends AFSFile {
    public AFSDirectory(ManagedHardDisk disk, String name, long start_block){
        super(disk, name, start_block);
        this.type = AFS_FILE_TYPE.DIRECTORY;
    }

    public AFSDirectory(AFSFile file){
        super(file);
        this.type = AFS_FILE_TYPE.DIRECTORY;
    }


    /* Precond:
     *  file is a valid AFSFile to add to the directory.
     *
     * Postcond:
     *  Returns true if the file was added.
     */
    public boolean add_file(AFSFile file){
        //Go to the end of the file.
        end();
        //Add the file to the listing
        if(!write_byte((byte)1))return false;
        if(!write(Conversions.string_to_bytes(file.get_name(), 160)))return false;
        if(!write_byte(file.get_type().val))return false;
        return write(Conversions.long_to_bytes(file.get_start()));
    }

    public boolean remove_file(String filename){
        long selected_location = -1;
        reset();
        String temp;
        while(read(1)[0] == 1){
            selected_location = file_pointer-1;
            temp = Conversions.bytes_to_string(read(160));
            advance(9);
            if(temp.equals(filename))break;
        }
        if(selected_location == -1)return true;
        long last_location = -1;
        while(read(1)[0] == 1){
            last_location = file_pointer-1;
            advance(160);
            advance(9);
        }
        if(last_location == -1)return true;
        //Swap the last file and the first file in the directory table
        seek(last_location);
        if(!write_byte((byte)0))return false; //Scrub the entry.
        byte[] last_name = read(160);
        byte last_type = read_byte();
        byte[] last_start = read(8);
        seek(selected_location);
        if(!write_byte((byte)1))return false;
        if(!write(last_name))return false;
        if(!write_byte(last_type))return false;
        return write(last_start);
    }


    /* Precond:
     *  None.
     *
     * Postcond:
     *  Returns the listing of all files in the directory.
     */
    public ArrayList<String> listing(){
        ArrayList<String> result = new ArrayList<>();
        reset();
        while(read(1)[0] == 1){
            result.add(Conversions.bytes_to_string(read(160)));
            advance(9);
        }
        return result;
    }

    /* Precond:
     *  filename is the name of the file to find the type of.
     *
     * Postcond:
     *  Returns the type of the file (if it exists in the directory)
     *  Returns type none if no such file exists.
     */
    public AFS_FILE_TYPE get_type(String filename){
        String temp;
        reset();
        while(read(1)[0] == 1){
            temp = Conversions.bytes_to_string(read(160));
            if(temp.equals(filename))return AFS_FILE_TYPE.from_byte(read(1)[0]);
            advance(9);
        }
        return AFS_FILE_TYPE.NONE;
    }

    /* Precond:
     *  filename is the name of the file to find the type of.
     *
     * Postcond:
     *  Returns starting block of the file.
     *  Returns -1 if the file cannot be located.
     */
    public long get_starting_block(String filename){
        String temp;
        reset();
        while(read(1)[0] == 1){
            temp = Conversions.bytes_to_string(read(160));
            if(temp.equals(filename)){
                advance(1);
                return Conversions.bytes_to_long(read(8));
            }
            advance(9);
        }
        return -1L;
    }

    /* Precond:
     *  filename is the name of the file to find the type of.
     *
     * Postcond:
     *  Returns true if the given filename is in the directory.
     */
    public boolean contains(String filename){
        String temp;
        reset();
        while(read(1)[0] == 1){
            temp = Conversions.bytes_to_string(read(160));
            if(temp.equals(filename))return true;
            advance(9);
        }
        return false;
    }
}
