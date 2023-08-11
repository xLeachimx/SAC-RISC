/* File: AFSTextFile.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 11 Aug 2023
 * Licence: GNU GPLv3
 * Purpose:
 *
 */


package com.atlantis.filesystem;

import com.atlantis.utilities.Conversions;
import com.hardware.ManagedHardDisk;

public class AFSTextFile extends AFSFile{
    public AFSTextFile(ManagedHardDisk disk, String name, long start_block){
        super(disk, name, start_block);
        this.type = AFS_FILE_TYPE.TEXT;
    }

    public AFSTextFile(AFSFile file){
        super(file);
        this.type = AFS_FILE_TYPE.TEXT;
    }


    public char readChar(){
        return Conversions.bytes_to_char(read(2));
    }

    public String readString(int length){
        StringBuilder result = new StringBuilder();
        char temp;
        for(int i = 0;i < length;i++){
            if(eof())break;
            temp = Conversions.bytes_to_char(super.read(2));
            result.append(temp);
        }
        return result.toString();
    }

    public String readLine(){
        StringBuilder result = new StringBuilder();
        char temp;
        do{
            temp = Conversions.bytes_to_char(super.read(2));
            if(temp != '\n')result.append(temp);
        }while(!eof() && temp != '\n');
        return result.toString();
    }
}
