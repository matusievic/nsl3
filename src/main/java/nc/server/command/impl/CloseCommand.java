package nc.server.command.impl;

import nc.server.command.ServerCommand;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.List;

public class CloseCommand implements ServerCommand {
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws IOException {}
}
