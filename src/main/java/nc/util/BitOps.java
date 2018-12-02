package nc.util;

public final class BitOps {
    public static short byteToShort(byte[] input) {
        return (short) ((input[0] & 0xff) << 8 | (input[1] & 0xff));
    }

    public static byte[] shortToByte(short input) {
        return new byte[]{(byte) ((input >> 8) & 0xff), (byte) (input & 0xff)};
    }
}
