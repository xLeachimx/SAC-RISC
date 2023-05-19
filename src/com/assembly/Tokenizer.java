/* File: Tokenizer.java
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

import java.util.ArrayList;

public class Tokenizer {
    //Enumeration of possible parsing states for tokenizing.
    private enum STATES{
        START,
        NUM_LIT_NEG,
        NUM_LIT,
        IDENT_LABEL,
        IN_STR,
        ESC
    }
    public enum RISC_TYPE{
        LABEL,
        IDENT,
        STRING,
        NUM
    }

    public static ArrayList<RISCToken> tokenize(String line, int line_num) throws AssemblyParseException{
        ArrayList<RISCToken> tokens = new ArrayList<>();
        if(line.isBlank())return tokens;
        StringBuilder temp = new StringBuilder();
        STATES state = STATES.START;
        for(int i = 0;i < line.length();i++){
            char current = line.charAt(i);
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
                        //Detect label or identifier.
                        else{
                            state = STATES.IDENT_LABEL;
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
                case IDENT_LABEL -> {
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
            }
        }
        //Handle what happens when the end of line is hit.
        switch (state){
            case NUM_LIT_NEG -> {
                String err_label = "Unexpected char end of number literal";
                throw new AssemblyParseException(err_label, line_num, 0);
            }
            case NUM_LIT -> {
                //Handle end of number
                tokens.add(new RISCToken(RISC_TYPE.NUM, temp.toString()));
            }
            case IDENT_LABEL -> {
                //Handle Identifier
                tokens.add(new RISCToken(RISC_TYPE.IDENT, temp.toString()));
            }
            case IN_STR, ESC -> {
                String err_label = "Incomplete string.";
                throw new AssemblyParseException(err_label, line_num, 0);
            }
        }
        return tokens;
    }
}
