/* File: HardwareTests.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 11 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.hardware;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class HardwareTests {
    private static final Random rng = new Random();
    private static Scanner redirected_out;
    private static PipedInputStream in_pipe;
    private static PipedOutputStream out_pipe;
    public static void main(String[] args) throws IOException {
        long seed = rng.nextLong();
        System.out.printf("Seed: %d\n", seed);
        rng.setSeed(seed);
        PrintStream temp = System.out;
        out_pipe = new PipedOutputStream();
        in_pipe = new PipedInputStream(out_pipe);
        redirected_out = new Scanner(in_pipe);
        System.setOut(new PrintStream(out_pipe));
        boolean RAM_status = RAM_test();
        boolean CPU_status = CPU_test();
        System.setOut(temp);
        if(RAM_status)System.out.println("RAM tests passed.");
        if(CPU_status)System.out.println("CPU tests passed.");
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
        cpu.execute_one_register((byte)0x1B, (byte)2);
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
        //NEG
        cpu.setRegister(CPU.pc, 0);
        cpu.setRegister((byte) 0, 10);
        cpu.execute_two_register((byte) 0x07, (byte) 0, (byte) 1);
        if(!cpu.check_register(CPU.pc, 3)){
            System.err.println("ERROR TWO ARGUMENT COMMANDS DO NOT PROPERLY ADVANCE THE PC.");
            passed = false;
        }
        if(!cpu.check_register((byte) 1, ~10)){
            System.err.println("NEGATION NO PERFORMED CORRECTLY.");
            System.err.printf("EXPECTED: %d\n", (~10));
            System.err.printf("GOT: %d\n", cpu.getRegisters()[1]);
            passed = false;
        }
        //LSHIFT
        cpu.setRegister((byte) 0, 10);
        cpu.execute_two_register((byte) 0x0A, (byte) 0, (byte) 1);
        if(!cpu.check_register((byte) 1, 20)){
            System.err.println("LEFT SHIFT NOT PERFORMED CORRECTLY.");
            System.err.printf("EXPECTED: %d\n", 20);
            System.err.printf("GOT: %d\n", cpu.getRegisters()[1]);
            passed = false;
        }
        //RSHIFT
        cpu.setRegister((byte) 0, 10);
        cpu.execute_two_register((byte) 0x0B, (byte) 0, (byte) 1);
        if(!cpu.check_register((byte) 1, 5)) {
            System.err.println("RIGHT SHIFT NOT PERFORMED CORRECTLY.");
            System.err.printf("EXPECTED: %d\n", 5);
            System.err.printf("GOT: %d\n", cpu.getRegisters()[1]);
            passed = false;
        }
        //BRANCH
        cpu.setRegister((byte) 0, 0);
        cpu.setRegister((byte) 1, 1);
        cpu.setRegister((byte) 3, 342);
        cpu.setRegister(CPU.pc, 0);
        cpu.execute_two_register((byte)0x0F, (byte) 0, (byte) 3);
        if(!cpu.check_register(CPU.pc, 3)){
            System.err.println("BRANCH DOES NOT WORK ON FALSE.");
            System.err.printf("Expected: %d\n", 3);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        cpu.execute_two_register((byte)0x0F, (byte) 1, (byte) 3);
        if(!cpu.check_register(CPU.pc, 342)){
            System.err.println("BRANCH DOES NOT WORK ON TRUE.");
            System.err.printf("Expected: %d\n", 342);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        //COPY
        cpu.setRegister((byte) 0, 100);
        cpu.setRegister((byte) 1, 0);
        cpu.execute_two_register((byte)0x11, (byte) 0, (byte) 1);
        if(!cpu.check_register((byte) 1, 100)){
            System.err.println("lCOPY DOES NOT WORK.");
            System.err.printf("Expected: %d\n", 100);
            System.err.printf("Got: %d\n", cpu.getRegisters()[1]);
            passed = false;
        }
        //LOAD
        ram.store_word(0, 238);
        cpu.setRegister((byte) 4, 0);
        cpu.execute_two_register((byte)0x17, (byte) 4, (byte) 5);
        if(!cpu.check_register((byte) 5, 238)){
            System.err.println("LOAD NOT WORKING.");
            System.err.printf("Expected: %d\n", 238);
            System.err.printf("Got: %d\n", cpu.getRegisters()[5]);
            passed = false;
        }
        //LOAD_BYTE
        ram.store_word(0, 257);
        cpu.setRegister((byte) 4, 3);
        cpu.execute_two_register((byte)0x18, (byte) 4, (byte) 5);
        if(!cpu.check_register((byte) 5, 1)){
            System.err.println("LOAD_BYTE NOT WORKING.");
            System.err.printf("Expected: %d\n", 1);
            System.err.printf("Got: %d\n", cpu.getRegisters()[5]);
            passed = false;
        }
        //STORE
        ram.store_word(0, 11111221);
        cpu.setRegister((byte) 0, 0);
        cpu.setRegister((byte) 1, 4505);
        cpu.execute_two_register((byte)0x19, (byte) 0, (byte) 1);
        if(ram.load_word(0) != 4505){
            System.err.println("ERROR WITH STORE.");
            System.err.printf("Expected: %d\n", 4505);
            System.err.printf("Got: %d\n", ram.load_word(0));
            passed = false;
        }
        //STORE_BYTE
        ram.store_word(0, 11111221);
        cpu.setRegister((byte) 0, 0);
        cpu.setRegister((byte) 1, 257);
        cpu.execute_two_register((byte)0x1A, (byte) 0, (byte) 1);
        if(ram.load_byte(0) != 1){
            System.err.println("ERROR WITH STORE_BYTE.");
            System.err.printf("Expected: %d\n", 1);
            System.err.printf("Got: %d\n", ram.load_byte(0));
            passed = false;
        }
        //CORE DUMP IGNORED
        //Triple Register Commands
        int math_arg_a = rng.nextInt(50);
        int math_arg_b = rng.nextInt(50);
        int result = 0;
        //ADD
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a + math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x03, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH ADD.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //SUB
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a - math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x04, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH SUB.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //MULT
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a * math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x05, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH MULT.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //DIV
        while(math_arg_b == 0)math_arg_b = rng.nextInt();
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a / math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x06, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH DIV.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //AND
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a & math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x08, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH AND.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //OR
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = math_arg_a | math_arg_b;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x09, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH OR.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //GT
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = (math_arg_a > math_arg_b) ? 1 : 0;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x0C, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH GT.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //LT
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = (math_arg_a < math_arg_b) ? 1 : 0;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x0D, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH LT.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }
        //EQ
        cpu.setRegister((byte)0, math_arg_a);
        cpu.setRegister((byte)1, math_arg_b);
        result = (math_arg_a == math_arg_b) ? 1 : 0;
        cpu.setRegister((byte)2, 0);
        cpu.execute_three_register((byte)0x0E, (byte) 0, (byte)1, (byte)2);
        if(!cpu.check_register((byte)2, result)){
            System.err.println("ERROR WITH EQ.");
            System.err.printf("Expected: %d\n", result);
            System.err.printf("Got: %d\n", cpu.getRegisters()[2]);
            passed = false;
        }

        //Number literal commands
        //LOAD_LIT
        int rng_lit = rng.nextInt();
        cpu.setRegister(CPU.rs,0);
        cpu.execute_number_literal((byte)0x1D, rng_lit);
        if(!cpu.check_register(CPU.rs, rng_lit)){
            System.err.println("ERROR WITH LOAD_LIT.");
            System.err.printf("Expected: %d\n", rng_lit);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.rs]);
            passed = false;
        }
        //JUMP_LABEL
        cpu.setRegister(CPU.pc,0);
        cpu.execute_number_literal((byte)0x1E, rng_lit);
        if(!cpu.check_register(CPU.pc, rng_lit)){
            System.err.println("ERROR WITH JUMP_LABEL.");
            System.err.printf("Expected: %d\n", rng_lit);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }

        //Register Literal Commands
        //BRANCH_LIT
        cpu.setRegister((byte) 0, 0);
        cpu.setRegister((byte) 1, 1);
        cpu.execute_register_literal((byte)0x1F, (byte) 1, 10);
        if(!cpu.check_register(CPU.pc, 10)){
            System.err.println("ERROR WITH BRANCH_LIT.");
            System.err.printf("Expected: %d\n", 10);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        cpu.execute_register_literal((byte)0x1F, (byte) 0, 20);
        if(cpu.check_register(CPU.pc, 20)){
            System.err.println("ERROR WITH BRANCH_LIT.");
            System.err.printf("Expected: %d\n", 16);
            System.err.printf("Got: %d\n", cpu.getRegisters()[CPU.pc]);
            passed = false;
        }
        //SET
        cpu.setRegister((byte) 0, 0);
        cpu.execute_register_literal((byte)0x20, (byte) 0, 10);
        if(!cpu.check_register((byte)0, 10)){
            System.err.println("ERROR WITH SET.");
            System.err.printf("Expected: %d\n", 10);
            System.err.printf("Got: %d\n", cpu.getRegisters()[0]);
            passed = false;
        }
        return passed;
    }
}
