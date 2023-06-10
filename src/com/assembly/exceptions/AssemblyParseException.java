/* File: AssemblyParseError.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly.exceptions;

public class AssemblyParseException extends AssemblyException{
    private int column;
    public AssemblyParseException(String label, int line, int char_num) {
        super(label, line);
        column = char_num;
        type = ExceptionTypes.PARSE_ERROR;
    }

    @Override
    public String toString() {
        return String.format("Error at column: %d\n", column) + super.toString();
    }
}
