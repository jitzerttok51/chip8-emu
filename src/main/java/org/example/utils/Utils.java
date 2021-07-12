package org.example.utils;

import java.nio.ByteBuffer;

public class Utils {

    public static short[] byteArrToShort(byte[] arr) {
        var result = new short[arr.length/2];
        for(int i=0; i<result.length; i++) {
            var s = (short) ((Byte.toUnsignedInt(arr[i*2]) << 8) + Byte.toUnsignedInt(arr[i*2+1]));
            result[i] = s;
        }

        return result;
    }

    public static short[] intArrAsShort(int[] arr) {
        var ret = new short[arr.length];
        for(int i=0; i<arr.length; i++) {
            ret[i] = (short) arr[i];
        }
        return ret;
    }


    public static byte[] shortArrAsByte(short[] arr) {
        var ret = new byte[arr.length];
        for(int i=0; i<arr.length; i++) {
            ret[i] = (byte) arr[i];
        }
        return ret;
    }

    public static byte[] shortArrToBytes(short[] arr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.length*2);
        for(var opcode : arr) {
            byteBuffer.putShort(opcode);
        }

        return byteBuffer.array();
    }
}
