package nc.server.command.impl;

import nc.server.command.ServerCommand;
import nc.util.AckListener;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadCommand implements ServerCommand {
    private String fileName;
    private FileInputStream input;
    private Map<Short, DatagramPacket> output;
    private AtomicInteger window;
    private AckListener ackListener;
    private short length;
    private long startTime;

    @Override
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws Exception {
        byte[] datagram = datagrams.get(0);
        if (!this.isFileExists(datagram)) {
            this.sendErrorPacket(server, address);
            System.out.println(address + " > Cannot download requested file");
            return;
        }

        System.out.println(address + " > DOWNLOAD " + fileName);
        this.prepareResources(server);

        DatagramPacket initPacket = this.createInitPacket(address);
        server.send(initPacket);

        byte[] buffer = new byte[126];
        short current = 1;
        short total = (short) Math.ceil((double) length / 126);
        int count;

        while ((count = input.read(buffer)) > 0) {
            this.waitForWindow(server);
            DatagramPacket packet = createPacket(buffer, current, total, count, address);
            server.send(packet);
            System.out.println("\tsending " + current + " / " + total);
            output.put(current++, packet);
            window.getAndIncrement();
        }

        while (window.get() != 0) {
            Thread.sleep(1000);
        }
        this.cleanResources(input, address);
    }

    private void sendErrorPacket(DatagramSocket server, SocketAddress address) throws IOException {
        byte[] datagram = new byte[200];
        datagram[0] = 7;
        DatagramPacket ack = new DatagramPacket(datagram, 0, datagram.length, address);
        server.send(ack);
    }

    private boolean isFileExists(byte[] content) {
        fileName = new String(Arrays.copyOfRange(content, 6, content.length)).split("\n")[0];
        return Files.isReadable(Paths.get("lab-2/server" + File.separator + fileName));
    }

    private DatagramPacket createInitPacket(SocketAddress address) {
        byte[] initData = new byte[200];
        initData[0] = 4;
        System.arraycopy((fileName + "\n").getBytes(), 0, initData, 6, fileName.length() + 1);
        return new DatagramPacket(initData, 0, 200, address);
    }

    private DatagramPacket createPacket(byte[] buffer, short current, short total, int count, SocketAddress address) {
        byte[] datagram = new byte[200];
        datagram[0] = 4;
        datagram[1] = (byte) (current >> 8);
        datagram[2] = (byte) current;
        datagram[3] = (byte) (total >> 8);
        datagram[4] = (byte) total;
        datagram[5] = (byte) count;
        System.arraycopy(buffer, 0, datagram, 6, count);
        return new DatagramPacket(datagram, 0, 200, address);
    }

    private void waitForWindow(DatagramSocket client) throws Exception {
        int sleepCount = 0;
        int maxSleepCount = 10;
        boolean flag = false;
        while (window.get() >= 10) {
            Thread.sleep(1000);
            sleepCount++;
            if (sleepCount >= maxSleepCount) {
                if (flag) {
                    System.out.println(client.getInetAddress() + " > INTERRUPTED");
                    ackListener.interrupt();
                    int last = output.values().stream().mapToInt(d -> {
                        byte[] data = d.getData();
                        return (short) (data[1] << 8 | data[2]);
                    }).min().orElse(0);
                    throw new Exception();
                }
                this.resendPackets(client);
                sleepCount = 0;
                flag = true;
            }
        }
    }

    private void resendPackets(DatagramSocket client) throws IOException {
        for (DatagramPacket packet : output.values()) {
            client.send(packet);
            byte[] datagram = packet.getData();
            short current = (short) (datagram[1] << 8 | datagram[2]);
            short total = (short) (datagram[3] << 8 | datagram[4]);
            System.out.println("\tresending " + current + " / " + total);
        }
    }

    private void prepareResources(DatagramSocket client) throws Exception {
        File file = new File("lab-2/server" + File.separator + fileName);
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new Exception(e);
        }
        length = (short) input.available();
        startTime = System.nanoTime();

        output = Collections.synchronizedMap(new HashMap<>(10));
        window = new AtomicInteger(0);
        ackListener = new AckListener(client, output, window);
        ackListener.start();
    }

    private void cleanResources(FileInputStream input, SocketAddress address) throws IOException {
        long finishTime = System.nanoTime();
        input.close();
        ackListener.interrupt();
        System.out.println(address + " > DOWNLOADING FINISHED");
        System.out.println("Bitrate: " + (length / ((finishTime - startTime) / 1000000000.0)) + " bps");
    }
}