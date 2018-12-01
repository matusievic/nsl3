package nc.server.runner;

import nc.server.Server;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run();
    }
}
