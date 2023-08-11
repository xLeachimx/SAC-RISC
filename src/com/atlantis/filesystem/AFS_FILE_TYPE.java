package com.atlantis.filesystem;

public enum AFS_FILE_TYPE {
    NONE(0xFF),
    DIRECTORY(0x00),
    GATE(0x01),
    DHD(0x02),
    TEXT(0x03),
    DATA(0x04);

    public final byte val;

    private AFS_FILE_TYPE(int val){
        this.val = (byte)val;
    }

    public static AFS_FILE_TYPE from_byte(byte value){
        switch(value){
            case 0x00 -> {return DIRECTORY;}
            case 0x01 -> {return GATE;}
            case 0x02 -> {return DHD;}
            case 0x03 -> {return TEXT;}
            case 0x04 -> {return DATA;}
            default -> {return NONE;}
        }

    }
}
