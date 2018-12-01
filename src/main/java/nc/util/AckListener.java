package nc.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AckListener extends Thread {
    private DatagramSocket client;
    private Map<Short, DatagramPacket> output;
    private AtomicInteger window;
    private boolean isInterrupted;

    public AckListener(DatagramSocket client, Map<Short, DatagramPacket> output, AtomicInteger window) {
        this.output = output;
        this.client = client;
        this.window = window;
    }

    @Override
    public void run() {
        byte[] buf = new byte[PacketConf.size];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            while (!isInterrupted) {
                try {
                    client.receive(packet);
                } catch (SocketTimeoutException e) {
                    if (window.get() <= 0) {
                        this.interrupt();
                    }
                    if (!isInterrupted) {
                        throw e;
                    }
                }
                byte[] packContent = packet.getData();
                if (packContent[PacketConf.operationOffset] == Operations.UPLOAD) {
                    short current = BitOps.byteToShort(Arrays.copyOfRange(packContent, PacketConf.currentOffset, PacketConf.currentOffset + 2));
                    short total = BitOps.byteToShort(Arrays.copyOfRange(packContent, PacketConf.totalOffset, PacketConf.totalOffset + 2));
                    System.out.println("\tack " + current + " / " + total);
                    output.remove(current);
                    window.getAndDecrement();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        this.isInterrupted = true;
    }
}
