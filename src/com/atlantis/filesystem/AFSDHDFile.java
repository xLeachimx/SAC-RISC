/* File: AFSDHDFile.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 11 Aug 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class for files holding SAC-RISC instructions for easier automatic compilation.
 */


package com.atlantis.filesystem;

import com.hardware.ManagedHardDisk;

public class AFSDHDFile extends AFSTextFile{

    public AFSDHDFile(ManagedHardDisk disk, String name, long start_block) {
        super(disk, name, start_block);
        this.type = AFS_FILE_TYPE.DHD;
    }

    public AFSDHDFile(AFSFile file){
        super(file);
        this.type = AFS_FILE_TYPE.DHD;
    }
}
