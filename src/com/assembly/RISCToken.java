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
    public RISCTokenizer.RISC_TYPE type;
    public String contents;

    //Extra parsing pass values
    public RISCToken(RISCTokenizer.RISC_TYPE type, String contents){
        this.type = type;
        if(type != RISCTokenizer.RISC_TYPE.STRING) {
            this.contents = contents.toUpperCase();
            if(this.type == RISCTokenizer.RISC_TYPE.IDENT) {
                for (RISCCommandList cmd : RISCCommandList.values()) {
                    if (this.contents.equals(cmd.toString())){
                        this.type = RISCTokenizer.RISC_TYPE.CMD;
                        break;
                    }
                }
            }
        }
        else{
            this.contents = process_escapes(contents);
        }
    }

    //Precond:
    //  other is a valid RISCToken object.
    //
    //Postcond:
    //  Returns true if both the contents and typrs are the same in both objects.
    public boolean equals(RISCToken other){
        return type == other.type && contents.equals(other.contents);
    }

    //Precond:
    //  None.
    //
    //Postcond:
    //  Returns a string representation of the token.
    public String toString(){
        StringBuilder temp = new StringBuilder(type.toString());
        temp.append(": ");
        temp.append(contents);
        return temp.toString();
    }

    //Precond:
    //  str is a valid string.
    //
    //Postcond:
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
