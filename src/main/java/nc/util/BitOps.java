package nc.util;

public final class BitOps {
    public static short byteToShort(byte[] input) {
        return (short) (input[0] << 8 | input[1]);
    }

    public static byte[] shortToByte(short input) {
        return new byte[]{(byte) (input >> 8), (byte) input};
    }
}
