/* File: conversions.java
 * Author: Dr. Michael Andrew Huelsman
 * Created On: 10 Aug 2023
 * Licence: GNU GPLv3
 * Purpose:
 *
 */


package com.atlantis.utilities;

import java.util.Arrays;

public class Conversions {
    public static final int SIGN_EXTENSION_MASK = 0xFF;
    private static final int LS_BYTE_MASK = 0xFF;

    public static int bytes_to_int(byte[] bytes){
        int result = 0;
        for(int i = 0;i < Integer.BYTES;i++){
            int byte_data = (int)bytes[i];
            byte_data = byte_data & SIGN_EXTENSION_MASK;
            result += byte_data;
            if(i < Integer.BYTES-1) result = result << Byte.SIZE;
        }
        return result;
    }

    public static byte[] int_to_bytes(int value){
        byte[] result = new byte[Integer.BYTES];
        for(int i = result.length-1;i >= 0;i--){
            byte temp = (byte)(value & LS_BYTE_MASK);
            result[i] = temp;
            value = value >> Byte.SIZE;
        }
        return result;
    }

    public static long bytes_to_long(byte[] bytes){
        int result = 0;
        for(int i = 0;i < Long.BYTES;i++){
            long byte_data = (long)bytes[i];
            byte_data = byte_data & SIGN_EXTENSION_MASK;
            result += byte_data;
            if(i < Long.BYTES-1) result = result << Byte.SIZE;
        }
        return result;
    }

    public static byte[] long_to_bytes(long value){
        byte[] result = new byte[Long.BYTES];
        for(int i = result.length-1;i >= 0;i--){
            byte temp = (byte)(value & LS_BYTE_MASK);
            result[i] = temp;
            value = value >> Byte.SIZE;
        }
        return result;
    }

    public static char bytes_to_char(byte[] bytes){
        char result = 0;
        for(int i = 0;i < Character.BYTES;i++){
            char byte_data = (char)bytes[i];
            byte_data = (char)(byte_data & SIGN_EXTENSION_MASK);
            result += byte_data;
            if(i < Character.BYTES-1) result = (char)(result << Byte.SIZE);
        }
        return result;
    }

    public static byte[] char_to_bytes(char value){
        byte[] result = new byte[Character.BYTES];
        for(int i = Character.BYTES-1;i >= 0;i--){
            byte temp = (byte)(value & LS_BYTE_MASK);
            result[i] = temp;
            value = (char)(value >> Byte.SIZE);
        }
        return result;
    }

    /* Precond:
     *  str is the string to encode as bytes.
     *  length is the static length of the string in characters plus 1.
     *
     * Postcond:
     *  Returns a byte sequence that represents the string as a null terminated string.
     */
    public static byte[] string_to_bytes(String str, int length){
        byte[] result = new byte[2*length];
        Arrays.fill(result, (byte)0x00);
        int limit = Math.min(2*str.length(), 2*length);
        for(int i = 0;i < str.length();i++){
            byte[] temp = char_to_bytes(str.charAt(i));
            result[2*i] = temp[0];
            result[2*i + 1] = temp[1];
        }
        return result;
    }

    public static String bytes_to_string(byte[] bytes){
        StringBuilder result = new StringBuilder();
        byte[] convert = new byte[Character.BYTES];
        char temp;
        for(int j, i = 0;i < bytes.length; i += Character.BYTES){
            for(j = 0; j < Character.BYTES;j++) {
                convert[j] = bytes[i + j];
            }
            temp = bytes_to_char(convert);
            if(temp != 0)result.append(temp);
            else break;
        }
        return result.toString();
    }
}
