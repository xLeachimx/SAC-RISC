/* File: CommandList.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 20 Mar 2023
 * Licence: GNU GPLv3
 * Purpose:
 * Notes:
 */


package com.assembly;

public enum CommandList {
    NOP(0x00),
    INPUT(0x01),
    INPUT_CHAR(0x02),
    HALT(0xFF),
    ADD(0x03),
    SUBT(0x04),
    MULT(0x05),
    DIV(0x06),
    NEG(0x07),
    AND(0x08),
    OR(0x09),
    LSHIFT(0x0A),
    RSHIFT(0x0B),
    GT(0x0C),
    LT(0x0D),
    EQ(0x0E),
    BRANCH(0x0F),
    JUMP(0x10),
    COPY(0x11),
    OUTPUT(0x12),
    OUTPUT_CHAR(0x13),
    PUSH_STK(0x14),
    POP_STK(0x15),
    SPLIT(0x16),
    LOAD(0x17),
    LOAD_BYTE(0x18),
    STORE(0x19),
    STORE_BYTE(0x1A),
    OUTPUT_STR(0x18),
    CAORE_DUMP(0x1C),
    LOAD_LIT(0x1D),
    JUMP_LIT(0x1E),
    LOAD_STR(0x1F);

    public final byte val;

    private CommandList(int val){
        this.val = (byte)val;
    }
}
