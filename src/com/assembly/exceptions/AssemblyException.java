/* File: AssemblyException.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 *  Generic base class exception for exceptions during assembly of SAC-RISC programs.
 * Notes:
 */


package com.assembly.exceptions;

public class AssemblyException extends Exception{
    private final String label;
    private final int line;
    protected ExceptionTypes type;
    public AssemblyException(String label, int line){
        type = ExceptionTypes.GENERIC;
        this.label = label;
        this.line = line;
    }

    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append(String.format("Error on linev %d\n", line));
        res.append(String.format("SAC-RISC Assembly Error of Type %s\n", type.toString()));
        res.append(String.format("Error Message:\n\t%s", label));
        return res.toString();
    }
}
