/* File: RISCToken.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 19 May 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  A data class for SAC RISC tokens
 * Notes:
 */


package com.assembly;

public class RISCToken {
    public Tokenizer.RISC_TYPE type;
    public String contents;
    public RISCToken(Tokenizer.RISC_TYPE type, String contents){
        this.type = type;
        if(type != Tokenizer.RISC_TYPE.STRING) {
            this.contents = contents.toUpperCase();
        }
        else{
            this.contents = process_escapes(contents);
        }
    }

    // Precond:
    //  str is a valid string.
    //
    // Postcond:
    //  Returns aa version of the string where escape characters have been properly applied.
    private String process_escapes(String str){
        StringBuilder result = new StringBuilder();
        for(int i = 0;i < str.length();i++){
            if(str.charAt(i) != '\\'){
                result.append(str.charAt(i));
            }
            else{
                i += 1;
                switch(str.charAt(i)){
                    case 'n' -> {
                        result.append("\n");
                    }
                    case 't' -> {
                        result.append("\t");
                    }
                    default -> {
                        result.append(str.charAt(i));
                    }
                }
            }
        }
        return result.toString();
    }
}
