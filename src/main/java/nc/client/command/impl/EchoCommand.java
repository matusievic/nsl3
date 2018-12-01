package nc.client.command.impl;

import nc.client.command.ClientCommand;
import nc.client.command.CommandProvider;
import nc.util.DataBuilder;
import nc.util.Operations;
import nc.util.PacketConf;
import nc.util.Types;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class EchoCommand implements ClientCommand {
    @Override
    public void execute(DatagramSocket client, String command) throws Exception {
        byte[] message = (command.substring(command.indexOf(' '))).getBytes();
        byte[] datagram = DataBuilder.build(Operations.ECHO, Types.PACKET, 0, 0, message);

        DatagramPacket packet = new DatagramPacket(datagram, datagram.length, CommandProvider.address, CommandProvider.port);
        client.send(packet);

        client.receive(packet);
        System.out.println("RESPONSE > " + new String(Arrays.copyOfRange(packet.getData(), PacketConf.payloadOffset, PacketConf.payloadOffset + message.length)));
    }
}
