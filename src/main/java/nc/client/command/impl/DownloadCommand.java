package nc.client.command.impl;

import nc.client.command.ClientCommand;
import nc.client.command.CommandProvider;

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

        byte[] data = new byte[200];
        DatagramPacket packet = new DatagramPacket(data, 0, data.length);
        Files.createFile(Paths.get("lab-2/client" + File.separator + fileName));
        output = new FileOutputStream("lab-2/client" + File.separator + fileName, true);

        int length;
        while (true) {
            client.receive(packet);
            byte[] header = packet.getData();
            short current = (short) (header[1] << 8 | header[2]);
            short total = (short) (header[3] << 8 | header[4]);

            output.write(Arrays.copyOfRange(header, 6, 6 + header[5]));

            header[0] = 5;
            DatagramPacket ack = new DatagramPacket(header, 0, header.length, CommandProvider.address, CommandProvider.port);
            client.send(ack);

            System.out.println("\treceiving " + current + " / " + total);
            if (current >= total) {
                length = total * 126;
                break;
            }
        }

        output.close();
        long finishTime = System.nanoTime();
        System.out.println("RESPONSE > SUCCESSFUL " + (length / ((finishTime - startTime) / 1000000000.0)) + " bps");
    }

    private DatagramPacket createInitPacket() {
        byte[] initData = new byte[200];
        initData[0] = 3;
        System.arraycopy((fileName + "\n").getBytes(), 0, initData, 6, fileName.length() + 1);
        return new DatagramPacket(initData, 200, CommandProvider.address, CommandProvider.port);
    }

    private boolean isFleExist(DatagramSocket client) throws IOException {
        byte[] buf = new byte[200];
        DatagramPacket packet = new DatagramPacket(buf, 200);
        client.receive(packet);
        return packet.getData()[0] != 7;
    }
}
