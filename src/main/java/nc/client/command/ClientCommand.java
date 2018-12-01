package nc.client.command;

import java.net.DatagramSocket;

public interface ClientCommand {
    void execute(DatagramSocket client, String command) throws Exception;
}
