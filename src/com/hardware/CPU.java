/* File: CPU.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 09 Feb 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A simulated 16-bit (address) CPU, with 24 bit commands.
 */


package com.hardware;

import java.util.Arrays;
import java.util.Scanner;

public class CPU {
    //Constants
    private static final int REGISTER_COUNT = 16;
    private static final byte ms_nibble = (byte)0xF0;
    private static final byte ls_nibble = (byte)0x0F;
    private static final short ls_byte = (short)0x00FF;
    private static final short ms_byte = (short)0xFF00;

    //Singleton Setup
    private static CPU instance = null;
    public CPU get_instance(){
        if(instance == null)instance = new CPU();
        return instance;
    }

    public void destroy_instance(){
        instance = null;
    }

    //Member data
    private final short[] registers = new short[REGISTER_COUNT + 4];; //General purpose registers.
    private final byte ra = 16; //Return address register.
    private final static byte sp = 17; //Stack point register.
    private final static byte pc = 18; //Program counter register.
    private final static byte rs = 19; //Intermediate result register.
    private boolean active;
    private final Scanner cin = new Scanner(System.in);

    //Member methods
    //Constructor
    private CPU(){
        Arrays.fill(registers, (short)0);
        active = false;
    }

    /*  Precond:
     *      cmd is a valid byte array with 3 bytes containing a valid command.
     *
     *  Postcond:
     *      Execture the given command updating the virtual machine.
    */
    public void step(){
        //Fetch
        byte[] cmd = new byte[3];
        cmd[0] = RAM.getInstance().load(registers[pc]);
        cmd[1] = RAM.getInstance().load(registers[pc]+1);
        cmd[2] = RAM.getInstance().load(registers[pc]+2);
        //Decode
        //Extract possible arguments
        byte cmd_code = cmd[0];
        byte[] reg = reg_split(cmd[1], cmd[2]);
        short literal = literal_comb(cmd[1], cmd[2]);
        //Execute
        switch(cmd_code){
            case 0x01 ->{
                //INPUT
                registers[rs] = cin.nextShort();
                cin.skip("\n");
            }
            case 0x02 -> {
                //INPUT_CHAR
                registers[rs] = (short)cin.nextLine().charAt(0);
            }
            case 0x03 -> {
                //ADD
                registers[reg[2]] = (short)(registers[reg[0]] + registers[reg[1]]);
            }
            case 0x04 -> {
                //SUBT
                registers[reg[2]] = (short)(registers[reg[0]] - registers[reg[1]]);
            }
            case 0x05 -> {
                //MULT
                registers[reg[2]] = (short)(registers[reg[0]] * registers[reg[1]]);
            }
            case 0x06 -> {
                //DIV
                registers[reg[2]] = (short)(registers[reg[0]] / registers[reg[1]]);
            }
            case 0x07 -> {
                //NEG
                registers[reg[1]] = (short)(~registers[reg[0]]);
            }
            case 0x08 -> {
                //AND
                registers[reg[2]] = (short)(registers[reg[0]] & registers[reg[1]]);
            }
            case 0x09 -> {
                //OR
                registers[reg[2]] = (short)(registers[reg[0]] | registers[reg[1]]);
            }
            case 0x0A -> {
                //LSHIFT
                registers[reg[1]] = (short)(registers[reg[0]] << 1);
            }
            case 0x0B -> {
                //RSHIFT
                registers[reg[1]] = (short)(registers[reg[0]] >> 1);
            }
            case 0x0C -> {
                //GT
                registers[reg[2]] = (short)((registers[reg[0]] > registers[reg[1]]) ? 1 : 0);
            }
            case 0x0D -> {
                //LT
                registers[reg[2]] = (short)((registers[reg[0]] < registers[reg[1]]) ? 1 : 0);
            }
            case 0x0E -> {
                //EQ
                registers[reg[2]] = (short)((registers[reg[0]] == registers[reg[1]]) ? 1 : 0);
            }
            case 0x0F -> {
                //BRANCH
                registers[pc] = registers[reg[0]] != 0 ? registers[reg[1]] : registers[pc];
            }
            case 0x10 -> {
                //JUMP
                registers[pc] = registers[reg[0]];
            }
            case 0x11 ->{
                //COPY
                registers[reg[0]] = registers[reg[1]];
            }
            case 0x12 -> {
                //OUTPUT
                System.out.println(registers[reg[0]]);
            }
            case 0x13 -> {
                //OUTPUT_CHAR
                char c = (char)(ls_byte & registers[reg[0]]);
                System.out.println(c);
            }
            case 0x14 -> {
                //PUSH_STK
                byte l_byte = (byte)((registers[reg[0]] & ms_byte) >> 4);
                byte r_byte = (byte)(registers[reg[0]] & ls_byte);
                RAM.getInstance().store(registers[sp], l_byte);
                RAM.getInstance().store(registers[sp]+1, r_byte);
                registers[sp] += 2;
            }
            case 0x15 -> {
                //POP_STK
                registers[sp] -= 2;
                byte l_byte = RAM.getInstance().load(registers[sp]);
                byte r_byte = RAM.getInstance().load(registers[sp]+1);
                registers[reg[0]] = literal_comb(l_byte, r_byte);
            }
            case 0x16 -> {
                //SPLIT
                byte l_byte = (byte)((registers[reg[0]] & ms_byte) >> 4);
                byte r_byte = (byte)(registers[reg[0]] & ls_byte);
                registers[reg[1]] = l_byte;
                registers[reg[2]] = r_byte;
            }
            case 0x17 -> {
                //LOAD
                byte l_byte = RAM.getInstance().load(registers[reg[0]]);
                byte r_byte = RAM.getInstance().load(registers[reg[0]]+1);
                registers[reg[1]] = literal_comb(l_byte, r_byte);
            }
            case 0x18 -> {
                //LOAD_BYTE
                registers[reg[1]] = RAM.getInstance().load(registers[reg[0]]);
            }
            case 0x19 -> {
                //STORE
                byte l_byte = (byte)((registers[reg[0]] & ms_byte) >> 4);
                byte r_byte = (byte)(registers[reg[0]] & ls_byte);
                RAM.getInstance().store(registers[reg[0]], l_byte);
                RAM.getInstance().store(registers[reg[0]]+1, r_byte);
            }
            case 0x1A -> {
                //STORE BYTE
                byte r_byte = (byte)(registers[reg[0]] & ls_byte);
                RAM.getInstance().store(registers[reg[0]], r_byte);
            }
            case 0x1B -> {
                //OUTPUT_STR
                short addr = registers[reg[0]];
                while(RAM.getInstance().load(addr) != 0){
                    System.out.print((char)RAM.getInstance().load(addr));
                    addr += 1;
                }
                System.out.println();
            }
            case 0x1C -> {
                //CORE_DUMP
                short addr = registers[reg[0]];
                for(int i = 0;i < registers[reg[1]];i++){
                    byte val = RAM.getInstance().load(addr+i);
                    System.out.print(val);
                    if(i % 8 == 0)System.out.println();
                    else System.out.print(" ");
                }
            }
            case 0x1D, 0x1F -> {
                //LOAD_LIT, LOAD_STR
                registers[rs] = literal;
            }
            case 0x1E -> {
                //JUMP_LIT
                registers[pc] = literal;
            }
            case (byte)0xFF -> {
                //HALT
                active = false;
            }
        }
        registers[pc] += 3;
    }

    /*  Precond:
     *      first is a valid byte containing up to two register indices.
     *      second is a valid byte containing one register index in it
     *          most significant nibble.
     *
     *  Postcond:
     *      Returns a byte array containing 3 elements each a register number.
     */
    private byte[] reg_split(byte first, byte second){
        byte[] regs = new byte[3];
        regs[0] = (byte)((first & ms_nibble) >> 4);
        regs[1] = (byte)((first & ls_nibble));
        regs[2] = (byte)((second & ms_nibble) >> 4);
        return regs;
    }

    /*  Precond:
     *      first is a valid byte containing the most significant 8-bits of a
     *          16-bit integer.
     *      second is a valid byte containing the least significant 8-bits of a
     *          16-bit integer.
     *
     *  Postcond:
     *      Combines the byte together into a single 16-bit integer.
     */
    private short literal_comb(byte first, byte second){
        return (short)((first << 8) + second);
    }

}
