/* File: AssemblyTypeException.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly.exceptions;

public class AssemblyTypeException extends AssemblyException{
    public AssemblyTypeException(String label, int line) {
        super(label, line);
        type = ExceptionTypes.TYPE_ERROR;
    }
}
