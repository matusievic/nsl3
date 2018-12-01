package nc.client.command;

import nc.client.command.impl.CloseCommand;
import nc.client.command.impl.DownloadCommand;
import nc.client.command.impl.EchoCommand;
import nc.client.command.impl.IgnoreCommand;
import nc.client.command.impl.TimeCommand;
import nc.client.command.impl.UploadCommand;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public final class CommandProvider {
    public static final CommandProvider instance = new CommandProvider();
    private final Map<String, ClientCommand> commands;
    private final ClientCommand ignoreCommand = new IgnoreCommand();
    public static InetAddress address;
    public static int port;

    private CommandProvider() {
        commands = new HashMap<>();

        commands.put("ECHO", new EchoCommand());
        commands.put("TIME", new TimeCommand());
        commands.put("CLOSE", new CloseCommand());

        commands.put("DOWNLOAD", new DownloadCommand());
        commands.put("UPLOAD", new UploadCommand());
    }

    public ClientCommand command(String command) {
        return commands.getOrDefault(command.toUpperCase(), ignoreCommand);
    }

    static {
        try {
            address = InetAddress.getByName("192.168.43.5");
        } catch (UnknownHostException ignored) {}
        port = 3345;
    }
}
