package nc.client.command.impl;

import nc.client.command.ClientCommand;
import nc.client.command.CommandProvider;
import nc.util.BitOps;
import nc.util.Operations;
import nc.util.PacketConf;
import nc.util.Types;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class DownloadCommand implements ClientCommand {
    private String fileName;
    private FileOutputStream output;
    private long startTime;

    @Override
    public void execute(DatagramSocket client, String command) throws Exception {
        startTime = System.nanoTime();
        fileName = command.split(" ")[1];

        DatagramPacket initPacket = this.createInitPacket();
        client.send(initPacket);

        if (!this.isFleExist(client)) {
            System.out.println("RESPONSE > There's no such file");
            return;
        }

        byte[] data = new byte[PacketConf.size];
        DatagramPacket packet = new DatagramPacket(data, 0, data.length);
        Files.createFile(Paths.get("client" + File.separator + fileName));
        output = new FileOutputStream("client" + File.separator + fileName, true);

        int length;
        while (true) {
            client.receive(packet);
            byte[] header = packet.getData();
            short current = BitOps.byteToShort(Arrays.copyOfRange(header, PacketConf.currentOffset, PacketConf.currentOffset + 2));
            short total = BitOps.byteToShort(Arrays.copyOfRange(header, PacketConf.totalOffset, PacketConf.totalOffset + 2));

            short count = BitOps.byteToShort(Arrays.copyOfRange(header, PacketConf.countOffset, PacketConf.countOffset + 2));
            output.write(Arrays.copyOfRange(header, PacketConf.payloadOffset, PacketConf.payloadOffset + count));

            header[PacketConf.typeOffset] = Types.ACKNOWLEDGE;
            DatagramPacket ack = new DatagramPacket(header, 0, header.length, CommandProvider.address, CommandProvider.port);
            client.send(ack);

            System.out.println("\treceiving " + current + " / " + total);
            if (current >= total) {
                length = total * PacketConf.payloadSize;
                break;
            }
        }

        output.close();
        long finishTime = System.nanoTime();
        System.out.println("RESPONSE > SUCCESSFUL " + (length / ((finishTime - startTime) / 1000000000.0)) + " bps");
    }

    private DatagramPacket createInitPacket() {
        byte[] initData = new byte[PacketConf.size];
        initData[PacketConf.operationOffset] = Operations.DOWNLOAD;
        System.arraycopy((fileName + "\n").getBytes(), 0, initData, 6, fileName.length() + 1);
        return new DatagramPacket(initData, initData.length, CommandProvider.address, CommandProvider.port);
    }

    private boolean isFleExist(DatagramSocket client) throws IOException {
        byte[] buf = new byte[PacketConf.size];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        client.receive(packet);
        client.receive(packet);
        return packet.getData()[PacketConf.operationOffset] != Operations.ERROR;
    }
}
