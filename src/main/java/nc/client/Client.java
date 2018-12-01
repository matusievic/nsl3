package nc.client;

import nc.client.command.CommandProvider;

import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public final class Client {
    private CommandProvider provider;
    private DatagramSocket client;
    private String hostname;
    private int port;

    public Client() {
        this.hostname = "192.168.43.5";
        this.port = 3345;
    }

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void run() throws Exception {
        client = new DatagramSocket();
        client.setSoTimeout(5000);
        provider = CommandProvider.instance;

        Scanner input = new Scanner(System.in);

        while (!client.isClosed() && input.hasNextLine()) {
            String command = input.nextLine();
            try {
                provider.command(command.split(" ")[0]).execute(client, command);
            } catch (SocketTimeoutException e) {
                System.out.println("RESPONSE > TIMEOUT");
            } catch (Exception e) {}
        }
    }
}
