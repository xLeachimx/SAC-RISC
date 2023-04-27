/* File: AssemblyParseError.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly.exceptions;

public class AssemblyParseException extends AssemblyException{
    public AssemblyParseException(String label, int line) {
        super(label, line);
        type = ExceptionTypes.PARSE_ERROR;
    }
}
