/* File: HardwareTests.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 11 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.hardware;

public class HardwareTests {
    public static void main(String[] args){
        RAM_test();
    }

    public static void RAM_test(){
        boolean passed = true;
        byte[] expected = {(byte)0xFF, (byte)0xAA, (byte)0xBB, (byte)0x11};
        RAM.getInstance().store_word(0, 0xFFAABB11);
        for(int i = 0;i < RAM.WORD_SIZE;i++){
            if(RAM.getInstance().load_byte(i) != expected[i]){
                System.err.println("BYTE STORAGE FAILED.");
                System.err.printf("GOT %X\n", RAM.getInstance().load_byte(i));
                System.err.printf("EXPECTED: %X\n", expected[i]);
                passed = false;
            }
        }
        if(RAM.getInstance().load_word(0) != 0xFFAABB11){
            System.err.println("WORD STORAGE FAILED.");
            System.err.printf("GOT %X\n", RAM.getInstance().load_word(0));
            System.err.printf("EXPECTED: %X\n", 0xFFAABB11);
            passed = false;
        }
        RAM.getInstance().store_byte(3, (byte)0x22);
        if(RAM.getInstance().load_byte(3) != (byte)0x22){
            System.err.println("BYTE STORAGE AT ADDRESS FAILED.");
            System.err.printf("GOT %X\n", RAM.getInstance().load_byte(3));
            System.err.printf("EXPECTED: %X\n", (byte)0x22);
            passed = false;
        }
        if(RAM.getInstance().load_word(0) != 0xFFAABB22){
            System.err.println("BYTE STORAGE AT ADDRESS FAILED.");
            System.err.printf("GOT %X\n", RAM.getInstance().load_word(0));
            System.err.printf("EXPECTED: %X\n", 0xFFAABB22);
            passed = false;
        }
        if(RAM.getInstance().load_word(1) != 0xAABB2200){
            System.err.println("BYTE ACCESSIBLE FAILED.");
            System.err.printf("GOT %X\n", RAM.getInstance().load_word(1));
            System.err.printf("EXPECTED: %X\n", 0xAABB2200);
            passed = false;
        }
        RAM.getInstance().store_char(5, 'C');
        if(RAM.getInstance().load_char(5) != 'C'){
            System.err.println("CHAR STORE/LOAD FAILED.");
            System.err.printf("GOT %s\n", RAM.getInstance().load_char(5));
            System.err.printf("EXPECTED: %s\n", 'C');
            passed = false;
        }
        if(passed){
            System.out.println("RAM working as intended.");
        }
    }
}
