/* File: RAM.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 09 Feb 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class which simulates RAM on a 8-bit machine.
 * Notes:
 *  RAM contains only 256 bytes.
 */


package com.hardware;

import java.util.Arrays;

public class RAM {
    private static final int SIZE = 1 << 16;
    private byte[] data = null;
    private static RAM instance = null;

    public static RAM getInstance(){
        if(instance == null){
            instance = new RAM();
        }
        return instance;
    }

    private RAM(){
        data = new byte[SIZE];
        Arrays.fill(data, (byte)0);
    }

    public byte load(int addr){
        return data[addr];
    }

    public void store(int addr, byte value){
        data[addr] = value;
    }
}
