package nc.server.command.impl;

import nc.server.command.ServerCommand;
import nc.util.DataBuilder;
import nc.util.Operations;
import nc.util.Types;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.List;

public class TimeCommand implements ServerCommand {
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws IOException {
        String date = LocalDateTime.now().toString();
        byte[] payload = date.getBytes();
        byte[] data = DataBuilder.build(Operations.TIME, Types.PACKET, 1, 1, payload);
        DatagramPacket packet = new DatagramPacket(data, data.length, address);
        server.send(packet);
        System.out.println(address + " > TIME");
    }
}
