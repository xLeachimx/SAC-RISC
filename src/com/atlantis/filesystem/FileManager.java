/* File: FileManager.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 16 Jul 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  FileManager acts as a gateway to the top-level directory system.
 * Notes:
 *  FileManager is a singleton class.
 */


package com.atlantis.filesystem;

import com.hardware.ManagedHardDisk;

public class FileManager {
    //Singleton Setup
    //Singleton Variables
    private static FileManager instance = null;

    //Singleton Methods
    public static FileManager create(ManagedHardDisk disk){
        if(instance != null)destroy();;
        instance = new FileManager(disk);
        instance.format();
        return instance;
    }

    public static FileManager open(ManagedHardDisk disk){
        if(instance != null)destroy();
        instance = new FileManager(disk);
        return instance;
    }

    public static FileManager getInstance(){
        return instance;
    }

    public static void destroy(){
        instance.close();
        instance = null;
    }

    //Instance Variables
    private ManagedHardDisk disk;

    private FileManager(ManagedHardDisk disk){
        this.disk = disk;
    }

    public void format(){
        //Stub
    }

    public void close(){
        //Stub
    }
}
