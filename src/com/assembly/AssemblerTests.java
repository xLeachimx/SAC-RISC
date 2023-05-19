/* File: AssemblerTests.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 19 May 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A class full of tests to ensure proper working of the Assembler and related components.
 * Notes:
 */


package com.assembly;

public class AssemblerTests {
    public static void main(String[] args){
        System.out.println("SAC-RISC Assembly Tests");
        if(TokenTester())System.out.println("RISCToken Tests Passed.");
        if(TokenizerTests())System.out.println("RISCTokenizer Tests Passed.");
    }

    //Tests the RISCToken class
    private static boolean TokenTester(){
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
        return passed;
    }

    //Tests for the RISCTokenizer class
    private static boolean TokenizerTests(){
        boolean passed = true;
        return passed;
    }
}