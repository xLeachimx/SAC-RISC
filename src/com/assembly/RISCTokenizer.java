/* File: RISCTokenizer.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 19 May 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class of static methods for tokenizing ASC RISC program lines.
 * Notes:
 *  Uses a FSA to parse a single line of SAC-RISC code.
 */


package com.assembly;

import com.assembly.exceptions.AssemblyParseException;
import com.hardware.CPU;

import java.util.ArrayList;

public class RISCTokenizer {
    //Enumeration of possible parsing states for tokenizing.
    private enum STATES{
        START,
        NUM_LIT_NEG,
        NUM_LIT,
        LABEL,
        REGISTER,
        IN_STR,
        ESC
    }

    //Enumeration of possible token types.
    public enum RISC_TYPE{
        LABEL,
        IDENT,
        STRING,
        NUM,
        CMD,
        REG
    }

    //Precond:
    //  line is a string containing a line of SAC-RISC code.
    //  line_num is the line number of the line of SAC-RISC code in the larger program.
    //
    //Postcond:
    //  Returns an ArrayList of RISCToken objects which represented the parsed line.
    //  If there is any error while parsing the line into tokens an AssemblyParseExecption is thrown.
    public static ArrayList<RISCToken> tokenize(String line, int line_num) throws AssemblyParseException{
        ArrayList<RISCToken> tokens = new ArrayList<>();
        if(line.isBlank())return tokens;
        StringBuilder temp = new StringBuilder();
        STATES state = STATES.START;
        for(int i = 0;i < line.length();i++){
            char current = line.charAt(i);
            //Consider and remove comments.
            if(line.charAt(i) == '#')break;
            switch (state){
                case START -> {
                    if(Character.isWhitespace(current))continue;
                    else if(current == '"')
                        state = STATES.IN_STR;
                    else {
                        temp.append(current);
                        //Detect number or negative number literal.
                        if (Character.isDigit(current))
                            state = STATES.NUM_LIT;
                        else if (current == '-')
                            state = STATES.NUM_LIT_NEG;
                        else if (current == '$')
                            state = STATES.REGISTER;
                        //Detect label or identifier.
                        else{
                            state = STATES.LABEL;
                        }
                    }
                }
                case NUM_LIT_NEG -> {
                    if(!Character.isDigit(current) || Character.isWhitespace(current)){
                        String err_label = String.format("Unexpected char %s in number.", current);
                        throw new AssemblyParseException(err_label, line_num, i);
                    }
                    temp.append(current);
                    state = STATES.NUM_LIT;
                }
                case NUM_LIT -> {
                    if(Character.isWhitespace(current)){
                        //Handle end of number
                        state = STATES.START;
                        tokens.add(new RISCToken(RISC_TYPE.NUM, temp.toString()));
                        temp.setLength(0);
                    }
                    else if(!Character.isDigit(current)){
                        String err_label = String.format("Unexpected char %s in number.", current);
                        throw new AssemblyParseException(err_label, line_num, i);
                    }
                    else {
                        temp.append(current);
                    }
                }
                case LABEL -> {
                    if(Character.isWhitespace(current)){
                        //Handle Identifier
                        state = STATES.START;
                        tokens.add(new RISCToken(RISC_TYPE.IDENT, temp.toString()));
                        temp.setLength(0);
                    }
                    else if(current == ':'){
                        //Handle Label
                        state = STATES.START;
                        tokens.add(new RISCToken(RISC_TYPE.LABEL, temp.toString()));
                        temp.setLength(0);
                    }
                    else if(current == '"' || current == '-'){
                        String err_label = String.format("Unexpected char %s in identifier.", current);
                        throw new AssemblyParseException(err_label, line_num, i);
                    }
                    else {
                        temp.append(current);
                    }
                }
                case IN_STR -> {
                    if(current == '"'){
                        //Handle string
                        state = STATES.START;
                        tokens.add(new RISCToken(RISC_TYPE.STRING, temp.toString()));
                        temp.setLength(0);
                    }
                    else if(current == '\\'){
                        //Handle escape characters
                        state = STATES.ESC;
                    }
                    else{
                        temp.append(current);
                    }
                }
                case ESC -> {
                    temp.append(current);
                    state = STATES.IN_STR;
                }
                case REGISTER -> {
                    if(Character.isWhitespace(current)){
                        state = STATES.START;
                        temp.deleteCharAt(0);
                        //Make sure the register is valid
                        String reg_val = temp.toString().toUpperCase();
                        String err_label = String.format("Unknown register %s.", reg_val);
                        try{
                            int val = Integer.parseInt(reg_val);
                            if(val < 0 || val > CPU.get_instance().numRegisters())
                                throw new AssemblyParseException(err_label, line_num, i);
                        }
                        catch(NumberFormatException exp){
                            boolean found = false;
                            for(String str : CPU.special_registers){
                                if(str.equals(reg_val)){
                                    found = true;
                                    break;
                                }
                            }
                            if(!found)
                                throw new AssemblyParseException(err_label, line_num, i);
                        }
                        tokens.add(new RISCToken(RISC_TYPE.REG, reg_val));
                        temp.setLength(0);
                    }
                    else
                        temp.append(current);
                }
            }
        }
        //Handle what happens when the end of line is hit.
        switch (state){
            case NUM_LIT_NEG -> {
                String err_label = "Unexpected char end of number literal";
                throw new AssemblyParseException(err_label, line_num, line.length()-1);
            }
            case NUM_LIT -> {
                //Handle end of number
                tokens.add(new RISCToken(RISC_TYPE.NUM, temp.toString()));
            }
            case LABEL -> {
                //Handle Identifier
                tokens.add(new RISCToken(RISC_TYPE.IDENT, temp.toString()));
            }
            case IN_STR, ESC -> {
                String err_label = "Incomplete string.";
                throw new AssemblyParseException(err_label, line_num, line.length()-1);
            }
            case REGISTER -> {
                //Make sure the register is valid
                temp.deleteCharAt(0);
                String reg_val = temp.toString().toUpperCase();
                String err_label = String.format("Unknown register %s.", reg_val);
                try{
                    int val = Integer.parseInt(reg_val);
                    if(val < 0 || val > CPU.get_instance().numRegisters())
                        throw new AssemblyParseException(err_label, line_num, line.length()-1);
                }
                catch(NumberFormatException exp){
                    boolean found = false;
                    for(String str : CPU.special_registers){
                        if(str.equals(reg_val)){
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        throw new AssemblyParseException(err_label, line_num, line.length()-1);
                }
                tokens.add(new RISCToken(RISC_TYPE.REG, reg_val));
                temp.setLength(0);
            }
        }
        return tokens;
    }
}
