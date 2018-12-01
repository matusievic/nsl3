package nc.server;

import nc.server.command.CommandProvider;
import nc.server.util.UploadQueue;
import nc.util.BitOps;
import nc.util.PacketConf;
import nc.util.Types;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Server {
    private Map<InetSocketAddress, List<byte[]>> datagrams;
    private CommandProvider provider;
    private DatagramSocket server;
    private DatagramPacket packet;
    private UploadQueue uploadQueue;
    private boolean interrupted;
    private int port;

    public Server() {
        this.port = 3345;
    }

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        datagrams = new HashMap<>();
        server = new DatagramSocket(port);
        server.setSoTimeout(1000);
        provider = CommandProvider.instance;
        uploadQueue = UploadQueue.instance(server);

        while (!interrupted) {
            uploadQueue.serve();
            byte[] buf = new byte[200];
            packet = new DatagramPacket(buf, 200);
            try {
                server.receive(packet);
            } catch (SocketTimeoutException ignored) {

            }
            try {
                processPacket(packet);
            } catch (Exception ignored) {
            }
        }
    }

    private void processPacket(DatagramPacket packet) throws Exception {
        InetSocketAddress address = (InetSocketAddress) packet.getSocketAddress();
        byte[] data = packet.getData();
        byte operation = data[PacketConf.operationOffset];
        byte type = data[PacketConf.typeOffset];
        short current = BitOps.byteToShort(Arrays.copyOfRange(data, PacketConf.currentOffset, PacketConf.currentOffset + 2));
        short total = BitOps.byteToShort(Arrays.copyOfRange(data, PacketConf.totalOffset, PacketConf.totalOffset + 2));

        System.out.println(address + " > " + current + " / " + total);
        if (type == Types.PACKET) {
            sendAck(address, data);
        } else if (type == Types.ACKNOWLEDGE) {
            uploadQueue.pop(packet);
        }

        if (current == 0) {
            ArrayList<byte[]> list = new ArrayList<>();
            list.add(data);
            datagrams.put(address, list);
        } else {
            datagrams.get(address).add(data);
        }

        if (current == total) {
            provider.command(operation).execute(server, address, datagrams.remove(address));
        }
    }

    private void sendAck(InetSocketAddress address, byte[] data) throws IOException {
        byte[] datagram = Arrays.copyOf(data, data.length);
        datagram[PacketConf.typeOffset] = Types.ACKNOWLEDGE;
        DatagramPacket packet = new DatagramPacket(datagram, 0, data.length, address);
        server.send(packet);
    }

    public void interrupt() {
        this.interrupted = true;
    }
}
