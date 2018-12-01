package nc.server.command.impl;

import nc.server.command.ServerCommand;
import nc.util.BitOps;
import nc.util.PacketConf;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

public class EchoCommand implements ServerCommand {
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte[] d : datagrams) {
            short count = BitOps.byteToShort(Arrays.copyOfRange(d, PacketConf.countOffset, PacketConf.countOffset + 2));
            sb.append(new String(Arrays.copyOfRange(d, PacketConf.payloadOffset, PacketConf.payloadOffset + count)));
        }
        System.out.println(address + " > " + sb.toString());
    }
}
