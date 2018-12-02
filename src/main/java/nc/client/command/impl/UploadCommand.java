package nc.client.command.impl;

import com.sun.xml.internal.ws.api.message.Packet;
import nc.client.command.ClientCommand;
import nc.client.command.CommandProvider;
import nc.util.AckListener;
import nc.util.BitOps;
import nc.util.DataBuilder;
import nc.util.Operations;
import nc.util.PacketConf;
import nc.util.Types;
import nc.util.WindowConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadCommand implements ClientCommand {
    private String fileName;
    private FileInputStream input;
    private Map<Short, DatagramPacket> output;
    private AtomicInteger window;
    private AckListener ackListener;
    private long length;
    private long startTime;

    @Override
    public void execute(DatagramSocket client, String command) throws Exception {
        this.prepareResources(client, command);
        byte[] buffer = new byte[PacketConf.payloadSize];

        short current = 1;
        short total = (short) Math.ceil(((double) length) / ((double) PacketConf.payloadSize));

        DatagramPacket initPacket = this.createInitPacket(total);
        client.send(initPacket);

        int count;
        while ((count = input.read(buffer)) > 0) {
            this.waitForWindow(client);
            DatagramPacket packet = createPacket(buffer, current, total, count);
            client.send(packet);
            output.put(current, packet);
            window.getAndIncrement();
            System.out.println("\tsending " + current + " / " + total);
            current++;
        }

        while (window.get() > 0) {
            Thread.sleep(1000);
        }
        cleanResources(input);
    }

    private DatagramPacket createInitPacket(short total) {
        byte[] initData = DataBuilder.build(Operations.UPLOAD, Types.PACKET, 0, total, fileName.getBytes());
        return new DatagramPacket(initData, PacketConf.size, CommandProvider.address, CommandProvider.port);
    }

    private DatagramPacket createPacket(byte[] buffer, short current, short total, int count) {
        byte[] datagram = DataBuilder.build(Operations.UPLOAD, Types.PACKET, current, total, buffer, count);
        return new DatagramPacket(datagram, PacketConf.size, CommandProvider.address, CommandProvider.port);
    }

    private void waitForWindow(DatagramSocket client) throws InterruptedException, IOException {
        int sleepCount = 0;
        while (window.get() >= 10) {
            Thread.sleep(1000);
            sleepCount++;
            if (sleepCount >= WindowConfig.maxSleepCount) {
                this.resendPackets(client);
                sleepCount = 0;
            }
        }
    }

    private void resendPackets(DatagramSocket client) throws IOException {
        for (DatagramPacket packet : output.values()) {
            byte[] datagram = packet.getData();
            short current = BitOps.byteToShort(Arrays.copyOfRange(datagram, PacketConf.currentOffset, PacketConf.currentOffset + 2));
            short total = BitOps.byteToShort(Arrays.copyOfRange(datagram, PacketConf.totalOffset, PacketConf.totalOffset + 2));
            client.send(packet);
            System.out.println("\tresending " + current + " / " + total);
        }
    }

    private void prepareResources(DatagramSocket client, String command) throws Exception {
        fileName = command.split(" ")[1];
        File file = new File("client" + File.separator + fileName);
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("RESPONSE > File not found");
            throw new Exception(e);
        }
        length = input.available();
        startTime = System.nanoTime();

        output = Collections.synchronizedMap(new HashMap<>(WindowConfig.size));
        window = new AtomicInteger(0);
        ackListener = new AckListener(client, output, window);
        ackListener.start();
    }

    private void cleanResources(FileInputStream input) throws IOException {
        long finishTime = System.nanoTime();
        input.close();
        ackListener.interrupt();
        System.out.println("RESPONSE > SUCCESSFUL " + (length / ((finishTime - startTime) / 1000000000.0)) + " bps");
    }
}
