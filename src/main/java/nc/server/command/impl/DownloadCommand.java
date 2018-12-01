package nc.server.command.impl;

import nc.server.command.ServerCommand;
import nc.server.util.UploadQueue;
import nc.util.DataBuilder;
import nc.util.Operations;
import nc.util.PacketConf;
import nc.util.Types;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class DownloadCommand implements ServerCommand {
    private String fileName;
    private FileInputStream input;
    private short length;
    private UploadQueue uploadQueue;

    @Override
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws Exception {
        uploadQueue = UploadQueue.instance();
        byte[] datagram = datagrams.get(0);
        if (!this.isFileExists(datagram)) {
            this.sendErrorPacket(server, address);
            System.out.println(address + " > Cannot download requested file");
            return;
        }

        System.out.println(address + " > DOWNLOAD " + fileName);
        this.prepareResources();

        datagram[PacketConf.typeOffset] = Types.ACKNOWLEDGE;
        DatagramPacket initPacket = new DatagramPacket(datagram, datagram.length, address);
        uploadQueue.push(initPacket);

        byte[] buffer = new byte[PacketConf.payloadSize];
        short current = 1;
        short total = (short) Math.ceil((double) length / PacketConf.payloadSize);
        int count;

        while ((count = input.read(buffer)) > 0) {
            byte[] data = DataBuilder.build(Operations.DOWNLOAD, Types.PACKET, current, total, buffer, count);
            DatagramPacket packet = new DatagramPacket(data, data.length, address);
            uploadQueue.push(packet);
            current++;
        }

        input.close();
    }

    private void sendErrorPacket(DatagramSocket server, SocketAddress address) throws IOException {
        byte[] datagram = DataBuilder.build(Operations.ERROR, Types.PACKET, 0, 0, null);
        DatagramPacket ack = new DatagramPacket(datagram, 0, datagram.length, address);
        uploadQueue.push(ack);
    }

    private boolean isFileExists(byte[] content) {
        fileName = new String(Arrays.copyOfRange(content, 6, content.length)).split("\n")[0];
        return Files.isReadable(Paths.get("server" + File.separator + fileName));
    }

    private void prepareResources() throws Exception {
        File file = new File("server" + File.separator + fileName);
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new Exception(e);
        }
        length = (short) input.available();
    }
}