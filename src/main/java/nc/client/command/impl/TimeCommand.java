package nc.client.command.impl;

import nc.client.command.ClientCommand;
import nc.client.command.CommandProvider;
import nc.util.BitOps;
import nc.util.DataBuilder;
import nc.util.Operations;
import nc.util.PacketConf;
import nc.util.Types;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class TimeCommand implements ClientCommand {
    @Override
    public void execute(DatagramSocket client, String command) throws Exception {
        byte[] datagram = DataBuilder.build(Operations.TIME, Types.PACKET, 0, 0,null);

        DatagramPacket packet = new DatagramPacket(datagram, datagram.length, CommandProvider.address, CommandProvider.port);
        client.send(packet);
        do {
            client.receive(packet);
        } while (packet.getData()[PacketConf.typeOffset] != 1);

        client.receive(packet);
        short count = BitOps.byteToShort(Arrays.copyOfRange(datagram, PacketConf.countOffset, PacketConf.countOffset + 2));
        System.out.println("RESPONSE > " + new String(Arrays.copyOfRange(datagram, PacketConf.payloadOffset, PacketConf.payloadOffset + count)));
    }
}
