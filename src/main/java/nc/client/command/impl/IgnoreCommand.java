package nc.client.command.impl;

import nc.client.command.ClientCommand;

import java.net.DatagramSocket;

public class IgnoreCommand implements ClientCommand {
    @Override
    public void execute(DatagramSocket client, String command) throws Exception {
        System.out.println("RESPONSE > Unrecognized command");
    }
}
