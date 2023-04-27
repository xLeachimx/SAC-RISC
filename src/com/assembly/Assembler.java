/* File: Assembler.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly;

import com.assembly.exceptions.AssemblyException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    //Singleton Variable
    private static Assembler instance = null;
    private static StringTokenizer tokenizer;

    //Singleton Methods
    public Assembler getInstance(){
        if(instance == null)instance = new Assembler();
        return instance;
    }

    //Class data
    private HashMap<String, Integer> label_table;
    private ArrayList<Short> literals;
    private ArrayList<String> str_literals;

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
        ArrayList<Byte> result = new ArrayList<>();
        int line_count = 0;
        while(fin.hasNextLine()){
            String line = fin.nextLine();
            line_count += 1;
            line = line.trim();
            if(line.isBlank())continue;
            if(line.charAt(0) == '#')continue;

        }
        fin.close();
        byte[] res_ary = new byte[result.size()];
        for(int i = 0;i < result.size();i++){
            res_ary[i] = result.get(i);
        }
        return res_ary;
    }

    private static String[] tokenizer(String line){
        ArrayList<String> res = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        boolean in_str = false;
        for(int i = 0;i < line.length();i++){
            if(line.charAt(i) == '\"'){
                in_str = !in_str;
            }
            if(in_str){
                temp.append(line.charAt(i));
            }
            else if(!line.substring(i, i+1).matches("\\s")){
                temp.append(line.charAt(i));
            }
            else{
                if(!temp.isEmpty())res.add(temp.toString());
                temp.delete(0, temp.length());
            }
        }
        if(!temp.isEmpty())res.add(temp.toString());
        return (String[])res.toArray();
    }
}
