package nc.server.command;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.List;

public interface ServerCommand {
    void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws Exception;
}
