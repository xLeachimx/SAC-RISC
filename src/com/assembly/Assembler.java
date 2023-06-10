/* File: Assembler.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly;

import com.assembly.exceptions.AssemblyException;
import com.assembly.exceptions.AssemblyParseException;
import com.assembly.exceptions.AssemblyUnknownArgumentException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Assembler {
    //Singleton Variable
    private static Assembler instance = null;

    //Singleton Methods
    public Assembler getInstance(){
        if(instance == null)instance = new Assembler();
        return instance;
    }

    //Class data
    private HashMap<String, Integer> label_table;
    private ArrayList<Integer> literals;
    private ArrayList<String> str_literals;
    private HashMap<Integer, Integer> line_line_table;
    private HashMap<Integer, Integer> line_pc_table;
    private HashSet<Integer> label_flag;

    //Class methods
    private Assembler(){
        label_table = new HashMap<>();
        literals = new ArrayList<>();
        str_literals = new ArrayList<>();
    }

    //Precond:
    //  filename is the name of a valid file which is to be assembled.
    //
    //Postcond:
    //  Returns a byte array consisting of the program in bytes.
    //  Throws a number of excptions if assembly or file I/O fails.
    public byte[] assemble(String filename) throws IOException, AssemblyException {
        File in_file = new File(filename);
        Scanner fin = new Scanner(in_file);
        ArrayList<ArrayList<String>> lines = new ArrayList<>();
        ArrayList<ArrayList<Byte>> result = new ArrayList<>();
        int current_line = 0;
        int parsable_lines = 0;
        while(fin.hasNextLine()){
            String line = fin.nextLine();
            line = line.trim();
            current_line += 1;
            if(line.isBlank())continue;
            if(line.charAt(0) == '#')continue;
            parsable_lines += 1;
            lines.add(tokenize(line));
            line_line_table.put(parsable_lines, current_line);
        }
        fin.close();
        for(int i = 0;i < lines.size();i++){
            if(lines.get(i).get(0).indexOf(':') != -1){
                String label = lines.get(i).get(0).replaceAll(":", "");
                label_table.put(label, i);
                lines.get(i).remove(0);
            }
        }
        //Populate label table
        //Parse lines and populate commands
        int line_match;
        for(int i = 0;i <  lines.size(); i++){
            result.add(line_to_bytes(lines.get(i), i));
        }
        ArrayList<Integer> line_byte = new ArrayList<>();
        int total_bytes = 0;
        for (ArrayList<Byte> bytes : result) {
            line_byte.add(total_bytes);
            total_bytes += bytes.size();
        }
        //Apply proper labels
        byte[] final_result = new byte[total_bytes];
        int ins_idx = 0;
        for(int i = 0;i < result.size();i++){
            //Make label changes
            if(label_flag.contains(i)){
                int literal = 0;
                for(int j = 1;j <= 5;j++){
                    literal += result.get(i).get(j);
                    if(j < 5)literal = literal << 8;
                }
                literal = line_byte.get(literal);
                for(int j = 5;j > 0;j--){
                    byte ls_byte = (byte)(literal & 0xFF);
                    result.get(i).set(j, ls_byte);
                    literal = literal >> 8;
                }
            }
            //Shift to final byte array
            for(int j = 0;j < result.get(i).size();j++){
                final_result[ins_idx+j] = result.get(i).get(j);
            }
            ins_idx += result.get(i).size();
        }
        return final_result;
    }

    private ArrayList<Byte> line_to_bytes(ArrayList<String> tokens, int line_num) throws AssemblyException {
        ArrayList<Byte> result = new ArrayList<>();
        int line_match;
        byte cmd = 0;
        try {
            cmd = RISCCommandList.valueOf(tokens.get(0).toUpperCase()).val;
        } catch (IllegalArgumentException exp){
            line_match = line_line_table.get(line_num);
            throw new AssemblyParseException("UNKNOWN COMMAND: " + tokens.get(0).toUpperCase(), line_match, 0);
        }
        int literal;
        switch(cmd) {
            //No argument commands
            case 0x00, 0x01, 0x02, (byte) 0xFF:
                result.add(cmd);
                break;
            //Single register commands
            case 0x10, 0x12, 0x13, 0x14, 0x15, 0x1B:
                result.add(cmd);
                result.add(parse_register(tokens.get(1)));
                break;
            //Double register commands
            case 0x07, 0x0A, 0x0B, 0x0F, 0x11, 0x17, 0x18, 0x19, 0x1A, 0x1C:
                result.add(cmd);
                result.add(parse_register(tokens.get(1)));
                result.add(parse_register(tokens.get(2)));
                break;
            //Triple register commands
            case 0x03, 0x04, 0x05, 0x06, 0x08, 0x09, 0x0C, 0x0D, 0x0E:
                result.add(cmd);
                result.add(parse_register(tokens.get(1)));
                result.add(parse_register(tokens.get(2)));
                result.add(parse_register(tokens.get(3)));
                break;
            //Number literal commands
            case 0x1D:
                result.add(cmd);
                literal = Integer.parseInt(tokens.get(1));
                for(int j = 0;j < 4;j++){
                    byte ls_byte = (byte)(literal & 0xFF);
                    result.add(ls_byte);
                    literal = literal >> 8;
                }
                break;
            case 0x1E:
                result.add(cmd);
                try{
                    literal = Integer.parseInt(tokens.get(1));
                } catch (NumberFormatException exp){
                    String label = tokens.get(1);
                    if(label_table.containsKey(label)){
                        literal = label_table.get(label);
                        label_flag.add(line_num);
                    }
                    else{
                        line_match = line_line_table.get(line_num);
                        throw new AssemblyUnknownArgumentException("UNKNOWN LABEL: " + label, line_match);
                    }
                }
                for(int j = 0;j < 4;j++){
                    byte ls_byte = (byte)(literal & 0xFF);
                    result.add(ls_byte);
                    literal = literal >> 8;
                }
                break;
            //Register literal commands
            case 0x1F:
                result.add(cmd);
                result.add(parse_register(tokens.get(1)));
                //Deal with labels
                try{
                    literal = Integer.parseInt(tokens.get(2));
                } catch (NumberFormatException exp){
                    String label = tokens.get(2);
                    if(label_table.containsKey(label)){
                        literal = label_table.get(label);
                        label_flag.add(line_num);
                    }
                    else{
                        line_match = line_line_table.get(line_num);
                        throw new AssemblyUnknownArgumentException("UNKNOWN LABEL: " + label, line_match);
                    }
                }
                for(int j = 0;j < 4;j++){
                    byte ls_byte = (byte)(literal & 0xFF);
                    result.add(ls_byte);
                    literal = literal >> 8;
                }
                break;
        }
        return result;
    }

    private ArrayList<String> tokenize(String line){
        StringBuilder current = new StringBuilder();
        ArrayList<String> result = new ArrayList<>();
        boolean in_string = false;
        for(int idx = 0;idx < line.length();idx++){
            if(!in_string) {
                if (line.charAt(idx) == ' ' && !current.isEmpty()) result.add(current.toString());
                else if(line.charAt(idx) == '"') {
                    if (!current.isEmpty()) result.add(current.toString());
                    in_string = true;
                }
                else if(line.charAt(idx) == ':') {
                    current.append(':');
                    result.add(current.toString());
                }
                else
                    current.append(line.charAt(idx));
            }
            else{
                if(line.charAt(idx) == '"'){
                    in_string = false;
                    result.add(current.toString());
                }
                else current.append(line.charAt(idx));
            }
        }
        if(!current.isEmpty()) result.add(current.toString());
        return result;
    }

    private byte parse_register(String str){
        switch(str){
            case "$ra" -> {
                return 16;
            }
            case "$sp" -> {
                return 17;
            }
            case "$pc" -> {
                return 18;
            }
            case "$rs" -> {
                return 19;
            }
            default -> {
                return Byte.parseByte(str);
            }
        }
    }
}
