/* File: CPU.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 09 Feb 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A simulated 32-bit CPU, with variable length commands.
 */


package com.hardware;

import java.util.Arrays;
import java.util.Scanner;

public class CPU {
    //Constants
    private static final int REGISTER_COUNT = 16;
    private static final int ls_byte = 0xFF;

    //Singleton Setup
    private static CPU instance = null;
    public static CPU get_instance(){
        if(instance == null)instance = new CPU();
        return instance;
    }

    public static void destroy_instance(){
        instance = null;
    }

    //Member data
    private final int[] registers = new int[REGISTER_COUNT + 4];; //General purpose registers.
    public final static byte ra = 16; //Return address register.
    public final static byte sp = 17; //Stack point register.
    public final static byte pc = 18; //Program counter register.
    public final static byte rs = 19; //Intermediate result register.
    private boolean active;
    private final Scanner cin = new Scanner(System.in);

    //Member methods
    //Constructor
    private CPU(){
        Arrays.fill(registers, 0);
        active = false;
    }

    //Testing, debug, and OS methods
    public boolean check_register(byte reg, int value){
        return registers[reg] == value;
    }

    public boolean is_active(){
        return active;
    }

    public void set_active(boolean value){
        active = value;
    }
    public int[] getRegisters(){
        return registers;
    }
    public void setRegister(byte reg, int value){
        registers[reg] = value;
    }

    /* Precond:
     *  None.
     *
     * Postcond:
     *  Fetches the command and all required information for executing it.
     */
    public void fetch_decode(){
        byte cmd = RAM.getInstance().load_byte(registers[pc]);
        byte reg1, reg2, reg3;
        int literal;
        switch(cmd){
            //No argument commands
            case 0x00, 0x01, 0x02, (byte)0xFF:
                execute_no_params(cmd);
                break;
            //Single register commands
            case 0x10, 0x12, 0x13, 0x14, 0x15, 0x1B:
                reg1 = RAM.getInstance().load_byte(registers[pc]+1);
                execute_one_register(cmd, reg1);
                break;
            //Double register commands
            case 0x07, 0x0A, 0x0B, 0x0F, 0x11, 0x17, 0x18, 0x19, 0x1A, 0x1C:
                reg1 = RAM.getInstance().load_byte(registers[pc]+1);
                reg2 = RAM.getInstance().load_byte(registers[pc]+2);
                execute_two_register(cmd, reg1, reg2);
                break;
            //Triple register commands
            case 0x03, 0x04, 0x05, 0x06, 0x08, 0x09, 0x0C, 0x0D, 0x0E:
                reg1 = RAM.getInstance().load_byte(registers[pc]+1);
                reg2 = RAM.getInstance().load_byte(registers[pc]+2);
                reg3 = RAM.getInstance().load_byte(registers[pc]+2);
                execute_three_register(cmd, reg1, reg2, reg3);
                break;
            //Number literal commands
            case 0x1D, 0x1E:
                literal = RAM.getInstance().load_word(registers[pc]+1);
                execute_number_literal(cmd, literal);
                break;
            //String literal commands
            case 0x1F:
                break;
            default:
                System.exit(100);
                
        }
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes no arguments.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by one byte.
     */
    public void execute_no_params(byte cmd){
        if(cmd == (byte)0xFF)active = false;
        registers[pc] += 1;
        if(cmd == 0x01)registers[rs] = cin.nextInt();
        else if(cmd == 0x02)registers[rs] = cin.nextLine().trim().charAt(0);
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes one register argument.
     *  reg is a int value indicating the index of the register to use.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by two byte.
     */
    public void execute_one_register(byte cmd, byte reg){
        if(reg < 0 || reg >= registers.length)System.exit(101);
        registers[pc] += 2;
        switch(cmd){
            case 0x10 -> {
                registers[pc] = registers[reg];
            }
            case 0x12 -> {
                System.out.println(registers[reg]);
            }
            case 0x13 -> {
                System.out.println((char)registers[reg]);
            }
            case 0x14 -> {
                RAM.getInstance().store_word(registers[sp], registers[reg]);
                registers[sp] += RAM.WORD_SIZE;
            }
            case 0x15 -> {
                registers[sp] -= RAM.WORD_SIZE;
                int temp = RAM.getInstance().load_word(registers[sp]);
                registers[reg] = temp;
            }
            case 0x1B -> {
                StringBuilder str = new StringBuilder();
                RAM ram = RAM.getInstance();
                int addr = registers[reg];
                while(ram.load_word(addr) != 0){
                    str.append((char)ram.load_char(addr));
                    addr += RAM.HWORD_SIZE;
                }
                System.out.println(str.toString());
            }
        }
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes two register argument.
     *  reg2 is a byte value indicating the index of the register to use.
     *  reg2 is a byte value indicating the index of the register to use.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by three byte.
     * 0x07, 0x0A, 0x0B, 0x0F, 0x11, 0x17, 0x18, 0x19, 0x1A, 0x1C
     */
    public void execute_two_register(byte cmd, byte reg1, byte reg2){
        if(reg1 < 0 || reg1 >= registers.length)System.exit(101);
        if(reg2 < 0 || reg2 >= registers.length)System.exit(101);
        registers[pc] += 3;
        switch(cmd){
            case 0x07 -> {
                registers[reg2] = ~registers[reg1];
            }
            case 0x0A -> {
                registers[reg2] = registers[reg1] << 1;
            }
            case 0x0B -> {
                registers[reg2] = registers[reg1] >> 1;
            }
            case 0x0F -> {
                if(registers[reg1] != 0)registers[pc] = registers[reg2];
            }
            case 0x11 -> {
                registers[reg2] = registers[reg1];
            }
            case 0x17 -> {
                registers[reg2] = RAM.getInstance().load_word(registers[reg1]);
            }
            case 0x18 -> {
                registers[reg2] = RAM.getInstance().load_byte(registers[reg1]);
            }
            case 0x19 -> {
                RAM.getInstance().store_word(registers[reg1], registers[reg2]);
            }
            case 0x1A -> {
                byte stored = (byte)(registers[reg2] & ls_byte);
                RAM.getInstance().store_byte(registers[reg1], stored);
            }
            case 0x1C -> {
                int addr = registers[reg1];
                for(int offset = 0; offset < registers[reg2];offset++){
                    System.out.printf("%X ", RAM.getInstance().load_byte(addr+offset));
                    if((offset % 10) == 0)System.out.println();
                }
            }
        }
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes three register argument.
     *  reg2 is a byte value indicating the index of the register to use.
     *  reg2 is a byte value indicating the index of the register to use.
     *  reg3 is a byte value indicating the index of the register to use.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by four bytes.
     */
    public void execute_three_register(byte cmd, byte reg1, byte reg2, byte reg3){
        if(reg1 < 0 || reg1 >= registers.length)System.exit(101);
        if(reg2 < 0 || reg2 >= registers.length)System.exit(101);
        if(reg3 < 0 || reg3 >= registers.length)System.exit(101);
        registers[pc] += 4;
        switch(cmd){
            case 0x03 -> {
                registers[reg3] = registers[reg1] + registers[reg2];
            }
            case 0x04 -> {
                registers[reg3] = registers[reg1] - registers[reg2];
            }
            case 0x05 -> {
                registers[reg3] = registers[reg1] * registers[reg2];
            }
            case 0x06 -> {
                registers[reg3] = registers[reg1] / registers[reg2];
            }
            case 0x08 -> {
                registers[reg3] = registers[reg1] & registers[reg2];
            }
            case 0x09 -> {
                registers[reg3] = registers[reg1] | registers[reg2];
            }
            case 0x0C -> {
                registers[reg3] = (registers[reg1] > registers[reg2]) ? 1 : 0;
            }
            case 0x0D -> {
                registers[reg3] = (registers[reg1] < registers[reg2]) ? 1 : 0;
            }
            case 0x0E -> {
                registers[reg3] = (registers[reg1] == registers[reg2]) ? 1 : 0;
            }
        }
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes one number literal argument.
     *  lit is the literal argument to the command.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by five bytes.
     */
    public void execute_number_literal(byte cmd, int lit){
        registers[pc] += 5;
        switch(cmd){
            case 0x1D -> {
                registers[rs] = lit;
            }
            case 0x1E -> {
                registers[pc] = lit;
            }
        }
    }

    /* Precond:
     *  cmd is a valid command code for the cpu which takes one number literal argument.
     *  reg is the register containing required information.
     *  lit is the literal argument to the command.
     *
     * Postcond:
     *  Executes the given commands and forwards the pc by six bytes.
     */
    public void execute_register_literal(byte cmd, byte reg, int lit){
        registers[pc] += 6;
        switch(cmd){
            case 0x1F -> {
                if(registers[reg] != 0)registers[pc] = lit;
            }
        }
    }
}
