/* File: RAM.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 09 Feb 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class which simulates RAM on a 32-bit machine.
 * Notes:
 *  RAM contains only 4096 bytes.
 *  RAM has a word length of 4 bytes.
 *  RAM is byte addressable.
 *  RAM is big-endian.
 */


package com.hardware;

import java.util.Arrays;

public class RAM {
    private static final int SIZE = 4096;
    public static final int HWORD_SIZE = 2;
    public static final int WORD_SIZE = 4;
    private static final int BYTE_SIZE = 8;
    public static final int SIGN_EXTENSION_MASK = 0xFF;
    private static final int LS_BYTE_MASK = 0xFF;
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

    public byte load_byte(int addr){
        return data[addr];
    }

    public char load_char(int addr){
        char result = 0;
        for(int i = 0;i < HWORD_SIZE;i++){
            char byte_data = (char)data[addr+i];
            byte_data = (char)(byte_data & SIGN_EXTENSION_MASK);
            result += byte_data;
            if(i < HWORD_SIZE-1) result = (char)(result << BYTE_SIZE);
        }
        return result;
    }

    public int load_word(int addr){
        int result = 0;
        for(int i = 0;i < WORD_SIZE;i++){
            int byte_data = (int)data[addr+i];
            byte_data = byte_data & SIGN_EXTENSION_MASK;
            result += byte_data;
            if(i < WORD_SIZE-1) result = result << BYTE_SIZE;
        }
        return result;
    }

    public void store_byte(int addr, byte value){
        data[addr] = value;
    }

    public void store_char(int addr, char value){
        for(int i = HWORD_SIZE-1;i >= 0;i--){
            byte temp = (byte)(value & LS_BYTE_MASK);
            data[addr+i] = temp;
            value = (char)(value >> BYTE_SIZE);
        }
    }

    public void store_string(int addr, String value){
        for(int i = 0;i < value.length();i++){
            store_char(addr+(HWORD_SIZE*i), value.charAt(i));
        }
        store_char(addr+value.length(), (char)0);
    }

    public void store_word(int addr, int value){
        for(int i = WORD_SIZE-1;i >= 0;i--){
            byte temp = (byte)(value & LS_BYTE_MASK);
            data[addr+i] = temp;
            value = value >> BYTE_SIZE;
        }
    }
}
