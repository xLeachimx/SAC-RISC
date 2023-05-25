/* File: RISCAssembler.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 19 May 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly;

import com.assembly.exceptions.AssemblyException;
import com.assembly.exceptions.AssemblyUnknownArgumentException;
import com.hardware.CPU;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RISCAssembler {
    //Classes for aid with conversions.
    private static class RISCLine{
        public RISCCommandList cmd;
        public int line_num;

        public RISCLine(RISCCommandList cmd, int line_num){
            this.cmd = cmd;
            this.line_num = line_num;
        }

        public ArrayList<Byte> getCode(){
            ArrayList<Byte> result = new ArrayList<>();
            result.add(cmd.val);
            return result;
        }

        public int byte_length(){
            return 1;
        }
    }

    private static class RISCSingleRegLine extends RISCLine{
        public byte register;

        public RISCSingleRegLine(RISCCommandList cmd, int line_num, byte register){
            super(cmd, line_num);
            this.register = register;
        }

        @Override
        public ArrayList<Byte> getCode() {
            ArrayList<Byte> result = super.getCode();
            result.add(register);
            return result;
        }

        @Override
        public int byte_length(){
            return super.byte_length() + 1;
        }
    }

    private static class RISCDoubleRegLine extends RISCSingleRegLine{
        public byte register;

        public RISCDoubleRegLine(RISCCommandList cmd, int line_num, byte register1, byte register2){
            super(cmd, line_num, register1);
            this.register = register2;
        }

        @Override
        public ArrayList<Byte> getCode() {
            ArrayList<Byte> result = super.getCode();
            result.add(register);
            return result;
        }

        @Override
        public int byte_length(){
            return super.byte_length() + 1;
        }
    }

    private static class RISCTripleRegLine extends RISCDoubleRegLine{
        public byte register;

        public RISCTripleRegLine(RISCCommandList cmd, int line_num, byte register1, byte register2, byte register3){
            super(cmd, line_num, register1, register2);
            this.register = register3;
        }

        @Override
        public ArrayList<Byte> getCode() {
            ArrayList<Byte> result = super.getCode();
            result.add(register);
            return result;
        }

        @Override
        public int byte_length(){
            return super.byte_length() + 1;
        }
    }

    private static class RISCLiteralLine extends RISCLine{
        public int literal;

        public RISCLiteralLine(RISCCommandList cmd, int line_num, int literal){
            super(cmd, line_num);
            this.literal = literal;
        }

        public void setLiteral(int num){
            this.literal = num;
        }

        @Override
        public ArrayList<Byte> getCode(){
            ArrayList<Byte> result = super.getCode();
            result.add((byte)((literal & 0xFF000000) >> 24));
            result.add((byte)((literal & 0x00FF0000) >> 16));
            result.add((byte)((literal & 0x0000FF00) >> 8));
            result.add((byte)(literal & 0x000000FF));
            return result;
        }

        @Override
        public int byte_length(){
            return super.byte_length() + 4;
        }
    }

    private static class RISCRegLiteralLine extends RISCSingleRegLine{
        public int literal;
        public RISCRegLiteralLine(RISCCommandList cmd, int line_num, byte register, int literal){
            super(cmd, line_num, register);
            this.literal = literal;
        }

        public void setLiteral(int num){
            this.literal = num;
        }

        @Override
        public ArrayList<Byte> getCode(){
            ArrayList<Byte> result = super.getCode();
            result.add((byte)((literal & 0xFF000000) >> 24));
            result.add((byte)((literal & 0x00FF0000) >> 16));
            result.add((byte)((literal & 0x0000FF00) >> 8));
            result.add((byte)(literal & 0x000000FF));
            return result;
        }

        @Override
        public int byte_length(){
            return super.byte_length() + 4;
        }
    }

    public static ArrayList<Byte> assemble_file(String filename) throws AssemblyException, IOException{
        File in_file = new File(filename);
        Scanner fin = new Scanner(in_file);
        ArrayList<String> program = new ArrayList<>();
        while(fin.hasNextLine()){
            program.add(fin.nextLine());
        }
        return assemble(program);
    }

    //Precond:
    //  program is a ArrayList of strings representing the lines of a SAC-RISC program.
    //
    //Postcond:
    //  Returns an ArrayList of bytes representing the assembled program.
    public static ArrayList<Byte> assemble(ArrayList<String> program) throws AssemblyException {
        ArrayList<Byte> result = new ArrayList<>();
        HashMap<String, Integer> labels = new HashMap<>();
        //Label Pass (Register labels)
        int line_num = 0;
        for(String line : program){
            line_num += 1;
            line = line.trim();
            if(line.isBlank())continue;
            ArrayList<RISCToken> tokens = RISCTokenizer.tokenize(line, line_num);
            if(tokens.size() == 0)continue;
            if (!valid_line(tokens))
                throw new AssemblyUnknownArgumentException("Unknown command-argument sequence.", line_num);
            int cmd_offset = 0;
            //Labels come first.
            while (cmd_offset < tokens.size() && tokens.get(cmd_offset).type == RISCTokenizer.RISC_TYPE.LABEL) cmd_offset += 1;
            for(int i = 0;i < cmd_offset;i++){
                labels.put(tokens.get(i).contents, line_num);
            }
        }
        //Basic Conversion Pass (Convert to RISCLine classes)
        ArrayList<RISCLine> lines = new ArrayList<>();
        line_num = 0;
        for(String line : program){
            int cmd_offset = 0;
            line_num += 1;
            if(line.isBlank())continue;
            ArrayList<RISCToken> tokens = RISCTokenizer.tokenize(line, line_num);
            if(tokens.size() == 0)continue;
            //Labels come first.
            while (cmd_offset < tokens.size() && tokens.get(cmd_offset).type == RISCTokenizer.RISC_TYPE.LABEL) cmd_offset += 1;
            //Add NOP to label only lines.
            if(cmd_offset >= tokens.size())tokens.add(new RISCToken(RISCTokenizer.RISC_TYPE.CMD, "NOP"));
            //Process Commands
            RISCCommandList cmd = RISCCommandList.valueOf(tokens.get(cmd_offset).contents);
            //Parsing variables
            byte reg1, reg2, reg3;
            int literal;
            String label;
            switch(cmd){
                case NOP, INPUT, INPUT_CHAR, HALT -> {
                    //No args
                    lines.add(new RISCLine(cmd, line_num));
                }
                case JUMP, OUTPUT, OUTPUT_CHAR, PUSH_STK, POP_STK, OUTPUT_STR -> {
                    //Single Register
                    reg1 = parseRegister(tokens.get(cmd_offset+1), line_num);
                    lines.add(new RISCSingleRegLine(cmd, line_num, reg1));
                }
                case NEG, LSHIFT, RSHIFT, BRANCH, COPY, LOAD, LOAD_BYTE,
                        STORE, STORE_BYTE, CORE_DUMP -> {
                    //Double Register
                    reg1 = parseRegister(tokens.get(cmd_offset+1), line_num);
                    reg2 = parseRegister(tokens.get(cmd_offset+2), line_num);
                    lines.add(new RISCDoubleRegLine(cmd, line_num, reg1, reg2));
                }
                case ADD, SUBT, MULT, DIV, AND, OR, GT, LT, EQ -> {
                    //Triple register
                    reg1 = parseRegister(tokens.get(cmd_offset+1), line_num);
                    reg2 = parseRegister(tokens.get(cmd_offset+2), line_num);
                    reg3 = parseRegister(tokens.get(cmd_offset+3), line_num);
                    lines.add(new RISCTripleRegLine(cmd, line_num, reg1, reg2, reg3));
                }
                case LOAD_LIT -> {
                    literal = Integer.parseInt(tokens.get(cmd_offset+1).contents);
                    lines.add(new RISCLiteralLine(cmd, line_num, literal));
                }
                case JUMP_LABEL -> {
                    label = tokens.get(cmd_offset+1).contents;
                    if(!labels.containsKey(label))throw new AssemblyUnknownArgumentException("Unknown label.", line_num);
                    literal = labels.get(label);
                    lines.add(new RISCLiteralLine(cmd, line_num, literal));
                }
                case BRANCH_LABEL -> {
                    reg1 = parseRegister(tokens.get(cmd_offset+1), line_num);
                    label = tokens.get(cmd_offset+2).contents;
                    if(!labels.containsKey(label))throw new AssemblyUnknownArgumentException("Unknown label.", line_num);
                    literal = labels.get(label);
                    lines.add(new RISCRegLiteralLine(cmd, line_num, reg1, literal));
                }
                case SET -> {
                    reg1 = parseRegister(tokens.get(cmd_offset+1), line_num);
                    literal = Integer.parseInt(tokens.get(cmd_offset+2).contents);
                    lines.add(new RISCRegLiteralLine(cmd, line_num, reg1, literal));
                }
            }
        }
        //Update lines/labels to correct byte offsets.
        HashMap<Integer, Integer> byteOffsets = new HashMap<>();
        int curr_byte = 0;
        for(RISCLine line : lines){
            byteOffsets.put(line.line_num, curr_byte);
            curr_byte += line.byte_length();
        }
        int j_num;
        for(RISCLine line : lines){
            if(line instanceof RISCLiteralLine && line.cmd == RISCCommandList.JUMP_LABEL){
                j_num = byteOffsets.get(((RISCLiteralLine) line).literal);
                ((RISCLiteralLine) line).literal = j_num;
            }
            else if(line instanceof RISCRegLiteralLine && line.cmd == RISCCommandList.BRANCH_LABEL){
                j_num = byteOffsets.get(((RISCRegLiteralLine) line).literal);
                ((RISCRegLiteralLine) line).literal = j_num;
            }
        }
        //Finally, build the program
        for(RISCLine line : lines){
            result.addAll(line.getCode());
        }
        return result;
    }

    //Precond:
    //  tokens is an ArrayList containing RISCToken objects.
    //
    //Postcond:
    //  Returns true if the order of the tokens makes a valid line in
    public static boolean valid_line(ArrayList<RISCToken> tokens){
        int cmd_offset = 0;
        //Labels come first.
        while(cmd_offset < tokens.size() && tokens.get(cmd_offset).type == RISCTokenizer.RISC_TYPE.LABEL)cmd_offset += 1;
        if(cmd_offset == tokens.size())return true;
        //Then Commands
        if(tokens.get(cmd_offset).type != RISCTokenizer.RISC_TYPE.CMD)return false;
        //Check Arguments
        int num_args = (tokens.size() - cmd_offset) - 1;
        switch(RISCCommandList.valueOf(tokens.get(cmd_offset).contents)){
            case NOP, INPUT, INPUT_CHAR, HALT -> {
                //No args
                return (num_args) == 0;
            }
            case JUMP, OUTPUT, OUTPUT_CHAR, PUSH_STK, POP_STK, OUTPUT_STR -> {
                //Single Register
                if(num_args != 1)return false;
                if(!registerToken(tokens.get(cmd_offset+1)))return false;
            }
            case NEG, LSHIFT, RSHIFT, BRANCH, COPY, LOAD, LOAD_BYTE,
                    STORE, STORE_BYTE, CORE_DUMP -> {
                //Double Register
                if(num_args != 2)return false;
                if(!registerToken(tokens.get(cmd_offset+1)))return false;
                if(!registerToken(tokens.get(cmd_offset+2)))return false;
            }
            case ADD, SUBT, MULT, DIV, AND, OR, GT, LT, EQ -> {
                //Triple register
                if(num_args != 3)return false;
                if(!registerToken(tokens.get(cmd_offset+1)))return false;
                if(!registerToken(tokens.get(cmd_offset+2)))return false;
                if(!registerToken(tokens.get(cmd_offset+3)))return false;
            }
            case LOAD_LIT -> {
                if(num_args != 1)return false;
                if(tokens.get(cmd_offset+1).type != RISCTokenizer.RISC_TYPE.NUM)return false;
            }
            case JUMP_LABEL -> {
                if(num_args != 1)return false;
                if(tokens.get(cmd_offset+1).type != RISCTokenizer.RISC_TYPE.IDENT)return false;
            }
            case BRANCH_LABEL -> {
                if(num_args != 2)return false;
                if(!registerToken(tokens.get(cmd_offset+1)))return false;
                if(tokens.get(cmd_offset+2).type != RISCTokenizer.RISC_TYPE.IDENT)return false;
            }
            case SET -> {
                if(num_args != 2)return false;
                if(!registerToken(tokens.get(cmd_offset+1)))return false;
                if(tokens.get(cmd_offset+2).type != RISCTokenizer.RISC_TYPE.NUM)return false;
            }
        }
        return true;
    }

    //Precond:
    //  token is a valid RISCToken object.
    //
    //Postcond:
    //  Returns true if the given token could be a valid register.
    private static boolean registerToken(RISCToken tk){
        return tk.type == RISCTokenizer.RISC_TYPE.REG || tk.type == RISCTokenizer.RISC_TYPE.NUM;
    }

    //Precond:
    //  token is a valid RISCToken object representing a register.
    //
    //Postcond:
    //  Returns true if the given token could be a valid register.
    private static byte parseRegister(RISCToken tk, int line_num) throws AssemblyUnknownArgumentException {
        if(tk.type == RISCTokenizer.RISC_TYPE.REG){
            switch (tk.contents) {
                case "RA" -> {
                    return CPU.ra;
                }
                case "RS" -> {
                    return CPU.rs;
                }
                case "SP" -> {
                    return CPU.sp;
                }
                case "PC" -> {
                    return CPU.pc;
                }
                default -> {
                    try{
                        return Byte.parseByte(tk.contents);
                    }
                    catch (NumberFormatException exp) {
                        throw new AssemblyUnknownArgumentException("Unknown register.", line_num);
                    }
                }
            }
        }
        return Byte.parseByte(tk.contents);
    }
}
