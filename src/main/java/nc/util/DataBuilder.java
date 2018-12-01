package nc.util;

public final class DataBuilder {
    public static byte[] build(byte operation, byte type, int current, int total, byte[] payload) {
        byte[] result = new byte[PacketConf.size];
        result[PacketConf.operationOffset] = operation;
        result[PacketConf.typeOffset] = type;
        System.arraycopy(BitOps.shortToByte((short) current), 0, result, PacketConf.currentOffset, 2);
        System.arraycopy(BitOps.shortToByte((short) total), 0, result, PacketConf.totalOffset, 2);
        if (payload != null) {
            System.arraycopy(BitOps.shortToByte((short) payload.length), 0, result, PacketConf.countOffset, 2);
            System.arraycopy(payload, 0, result, PacketConf.payloadOffset, payload.length);
        }
        return result;
    }

    public static byte[] build(byte operation, byte type, int current, int total, byte[] payload, int count) {
        byte[] result = new byte[PacketConf.size];
        result[PacketConf.operationOffset] = operation;
        result[PacketConf.typeOffset] = type;
        System.arraycopy(BitOps.shortToByte((short) current), 0, result, PacketConf.currentOffset, 2);
        System.arraycopy(BitOps.shortToByte((short) total), 0, result, PacketConf.totalOffset, 2);
        if (payload != null) {
            System.arraycopy(BitOps.shortToByte((short) count), 0, result, PacketConf.countOffset, 2);
            System.arraycopy(payload, 0, result, PacketConf.payloadOffset, payload.length);
        }
        return result;
    }
}
