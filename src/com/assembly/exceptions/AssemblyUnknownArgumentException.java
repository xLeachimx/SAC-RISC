/* File: AssemblyUnknownArgumentException.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly.exceptions;

public class AssemblyUnknownArgumentException extends AssemblyException{
    public AssemblyUnknownArgumentException(String label, int line) {
        super(label, line);
        type = ExceptionTypes.UNKNOWN_ARGUMENT;
    }
}
