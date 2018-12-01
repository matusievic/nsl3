package nc.server.util;

import nc.util.PacketConf;
import nc.util.Types;
import nc.util.WindowConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UploadQueue {
    private static UploadQueue instance;
    private DatagramSocket socket;
    private List<DatagramPacket> queue;
    private List<DatagramPacket> output;
    private int resetCounter = 0;

    private UploadQueue(DatagramSocket socket) {
        this.socket = socket;
        this.queue = new ArrayList<>();
        this.output = new ArrayList<>();
    }

    public void serve() throws IOException {
        if (queue.isEmpty()) {
            return;
        }

        if (resetCounter >= 5) {
            output.clear();
            resetCounter = 0;
        }

        if (output.size() < WindowConfig.size) {
            DatagramPacket packet = queue.remove(0);
            socket.send(packet);
            output.add(packet);
        } else {
            resetCounter++;
        }
    }

    public void push(DatagramPacket packet) {
        queue.add(packet);
    }

    public void pop(DatagramPacket packet) {
        byte[] data = packet.getData();
        data[PacketConf.typeOffset] = Types.PACKET;
        for (int i = 0; i < output.size(); i++) {
            DatagramPacket p = output.get(i);
            if (Arrays.equals(p.getData(), data)) {
                output.remove(i);
            }
        }
    }

    public static UploadQueue instance(DatagramSocket socket) {
        UploadQueue.instance = new UploadQueue(socket);
        return instance;
    }

    public static UploadQueue instance() {
        return instance;
    }
}
