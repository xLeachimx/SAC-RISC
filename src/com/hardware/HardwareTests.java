/* File: HardwareTests.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 11 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.hardware;

import java.io.*;
import java.util.Scanner;

public class HardwareTests {
    private static Scanner redirected_out;
    private static PipedInputStream in_pipe;
    private static PipedOutputStream out_pipe;
    public static void main(String[] args) throws IOException {
        out_pipe = new PipedOutputStream();
        in_pipe = new PipedInputStream(out_pipe);
        redirected_out = new Scanner(in_pipe);
        System.setOut(new PrintStream(out_pipe));
        boolean RAM_status = RAM_test();
        boolean CPU_status = CPU_test();
    }

    public static boolean RAM_test(){
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
        return passed;
    }

    public static boolean CPU_test(){
        boolean passed = true;
        CPU cpu = CPU.get_instance();
        RAM ram = RAM.getInstance();
        cpu.set_active(true);
        //No Params
        //NOOP
        cpu.setRegister(CPU.pc, 0);
        cpu.execute_no_params((byte)0x00);
        if(!cpu.check_register(CPU.pc, 1)){
            System.err.println("ERROR PC DOES NOT ADVANCE ON NO PARAM.");
            System.err.println("EXPECTED: 1");
            System.err.printf("GOT: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        //HALT
        cpu.execute_no_params((byte)0xFF);
        if(cpu.is_active()){
            System.err.println("ERROR WITH HALT COMMAND.");
            passed = false;
        }
        cpu.set_active(true);
        //Single register
        //JUMP
        cpu.setRegister((byte)0, 100);
        cpu.execute_one_register((byte)0x10, (byte)0);
        if(!cpu.check_register((byte)0, 100)){
            System.err.println("ERROR WITH JUMP COMMAND");
            passed = false;
        }
        //Check advancement of PC
        if(!cpu.check_register(CPU.pc, 100)){
            System.err.println("ERROR PC DOES NOT ADVANCE ON NO PARAM.");
            System.err.println("EXPECTED: 100");
            System.err.printf("GOT: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        //PUSH_STK
        cpu.setRegister((byte)0, 128);
        cpu.setRegister(CPU.sp, 5);
        cpu.execute_one_register((byte)0x14, (byte)0);
        if(!cpu.check_register(CPU.sp, 5+RAM.WORD_SIZE)){
            System.err.println("ERROR STACK POINTER NOT ADVANCED.");
            passed = false;
        }
        if(ram.load_word(5) != 128){
            System.err.println("ERROR PUSH_STK NOT STORING PROPERLY.");
            passed = false;
        }
        //POP_STK
        cpu.setRegister((byte)1, 12);
        cpu.execute_one_register((byte)0x15, (byte)1);
        if(!cpu.check_register((byte)1, 128)){
            System.err.println("ERROR POP_STK NOT RETRIEVING PROPERLY.");
            passed = false;
        }
        if(!cpu.check_register(CPU.sp, 5)){
            System.err.println("ERROR STACK POINTER NOT REVERTED.");
            passed = false;
        }
        //OUTPUT_STR
        ram.store_string(10, "Hello, World!");
        cpu.setRegister((byte)2, 10);
        cpu.execute_one_register((byte)0x18, (byte)2);
        System.out.flush();
        if(!redirected_out.hasNextLine()){
            System.err.println("ERROR STRING NOT PROPERLY PRINTED.");
            passed = false;
        } else {
            String output = redirected_out.nextLine();
            if (!output.equals("Hello, World!")) {
                System.err.println("ERROR INCORRECT STRING PRINTED!");
                System.err.printf("EXPECTED: %s\n", "Hello, World!");
                System.err.printf("GOT: %s\n", output);
                passed = false;
            }
        }
        //Two register commands
        return passed;
    }
}
