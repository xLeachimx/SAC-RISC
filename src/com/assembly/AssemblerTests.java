/* File: AssemblerTests.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 19 May 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class full of tests to ensure proper working of the Assembler and related components.
 * Notes:
 */


package com.assembly;

import com.assembly.exceptions.AssemblyException;

import java.util.ArrayList;
import java.util.List;

public class AssemblerTests {
    public static void main(String[] args){
        System.out.println("SAC-RISC Assembly Tests");
        if(tokenTests())System.out.println("RISCToken Tests Passed.");
        if(tokenizerTests())System.out.println("RISCTokenizer Tests Passed.");
        if(assemblerTests())System.out.println("RISCAssembler Tests Passed.");
    }

    //Tests the RISCToken class
    private static boolean tokenTests(){
        boolean passed = true;
        RISCToken temp = new RISCToken(RISCTokenizer.RISC_TYPE.STRING, "a\\nb");
        if(!temp.contents.equals("a\nb")){
            System.err.println("RISCToken class not parsing escape characters.");
            System.err.println("Expected: a\nb\n");
            System.err.printf("Got: %s\n", temp.contents);
            passed = false;
        }
        temp = new RISCToken(RISCTokenizer.RISC_TYPE.LABEL, "label1");
        if(!temp.contents.equals("LABEL1")){
            System.err.println("RISCToken class not upper case characters.");
            System.err.println("Expected: LABEL1");
            System.err.printf("Got: %s\n", temp.contents);
            passed = false;
        }
        temp = new RISCToken(RISCTokenizer.RISC_TYPE.IDENT, "AdD");
        if(temp.type != RISCTokenizer.RISC_TYPE.CMD){
            System.err.println("RISCToken class not recognizing commands..");
            System.err.printf("Expected: %s\n",RISCCommandList.ADD.toString());
            System.err.printf("Got: %s\n", temp.type.toString());
            passed = false;
        }
        return passed;
    }

    //Tests for the RISCTokenizer class
    private static boolean tokenizerTests(){
        boolean passed = true;
        //Test line
        String line = "label: add $5 $rs $ra";
        //Tokens associated with the line
        RISCToken label_tk = new RISCToken(RISCTokenizer.RISC_TYPE.LABEL, "LABEL");
        RISCToken cmd_tk = new RISCToken(RISCTokenizer.RISC_TYPE.CMD, "ADD");
        RISCToken reg1_tk = new RISCToken(RISCTokenizer.RISC_TYPE.REG, "5");
        RISCToken reg2_tk = new RISCToken(RISCTokenizer.RISC_TYPE.REG, "rs");
        RISCToken reg3_tk = new RISCToken(RISCTokenizer.RISC_TYPE.REG, "ra");
        try {
            ArrayList<RISCToken> tokens = RISCTokenizer.tokenize(line, 0);
            if(tokens.size() != 5){
                System.err.println("INCORRECT NUMBER OF TOKENS.");
                System.err.printf("EXPECTED: %d\n", 5);
                System.err.printf("GOT: %d\n", tokens.size());
                passed = false;
            }
            else if(!tokens.get(0).equals(label_tk)){
                System.err.println("ERROR TOKENIZING LABEL.");
                System.err.printf("EXPECTED: %s\n", label_tk);
                System.err.printf("GOT: %s\n", tokens.get(0));
                passed = false;
            }
            else if(!tokens.get(1).equals(cmd_tk)){
                System.err.println("ERROR TOKENIZING COMMAND.");
                System.err.printf("EXPECTED: %s\n", cmd_tk);
                System.err.printf("GOT: %s\n", tokens.get(1));
                passed = false;
            }
            else if(!tokens.get(2).equals(reg1_tk)){
                System.err.println("ERROR TOKENIZING REGISTER.");
                System.err.printf("EXPECTED: %s\n", reg1_tk);
                System.err.printf("GOT: %s\n", tokens.get(2));
                passed = false;
            }
            else if(!tokens.get(3).equals(reg2_tk)){
                System.err.println("ERROR TOKENIZING REGISTER.");
                System.err.printf("EXPECTED: %s\n", reg2_tk);
                System.err.printf("GOT: %s\n", tokens.get(3));
                passed = false;
            }
            else if(!tokens.get(4).equals(reg3_tk)){
                System.err.println("ERROR TOKENIZING REGISTER.");
                System.err.printf("EXPECTED: %s\n", reg3_tk);
                System.err.printf("GOT: %s\n", tokens.get(4));
                passed = false;
            }
        }
        catch(AssemblyException exp){
            System.err.println("COULD NOT TOKENIZE LINE.");
            System.err.println(exp);
            passed = false;
        }
        return passed;
    }

    //Tests for the assembler
    private static boolean assemblerTests(){
        boolean passed = true;
        String program_raw =
                """
                   #Initialize Loop Constants
                   SET $1 5
                   LOAD_LIT 32
                   ADD $5 $rs $5
                   LOOP:
                   # Print the sum of 5 and 32
                   Output $5
                   # Make an infinite loop
                   JUMP_LABEL LOOP
                        """;
        ArrayList<String> program = new ArrayList<String>(List.of(program_raw.split("\n")));
        byte[] assemlbed = {(byte)0x20, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05,
                            (byte)0x1D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20,
                            (byte)0x03, (byte)0x05, (byte)0x13, (byte)0x05,
                            (byte)0x00,
                            (byte)0x12, (byte)0x05,
                            (byte)0x1E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0F};
        ArrayList<Byte> attempt = null;
        try {
            attempt = RISCAssembler.assemble(program);
        } catch (AssemblyException exp){
            passed = false;
            System.err.println(exp);
        }
        if(attempt == null){
            System.err.println("PROBLEM WITH ASSEMBLY, SEE ERROR ABOVE.");
            passed = false;
        }
        else if(attempt.size() != assemlbed.length){
            System.err.println("ASSEMBLED PROGRAM NOT OF PROPER LENGTH.");
            System.err.printf("EXPECTED: %d\n", assemlbed.length);
            System.err.printf("GOT: %d\n", attempt.size());
        }
        else{
            for(int i = 0;i < attempt.size();i++){
                if(attempt.get(i) != assemlbed[i]){
                    passed = false;
                    System.err.printf("INCORRECT BYTE-CODE AT BYTE %d\n", i);
                    System.err.printf("ExPECTED: %d\n", assemlbed[i]);
                    System.err.printf("GOT: %d\n", attempt.get(i));
                }
            }
        }
        return passed;
    }
}