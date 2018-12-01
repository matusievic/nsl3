package nc.server.command;

import nc.server.command.impl.CloseCommand;
import nc.server.command.impl.DownloadCommand;
import nc.server.command.impl.EchoCommand;
import nc.server.command.impl.IgnoreCommand;
import nc.server.command.impl.TimeCommand;
import nc.server.command.impl.UploadCommand;

import java.util.HashMap;
import java.util.Map;

public final class CommandProvider {
    public static final CommandProvider instance = new CommandProvider();
    private final Map<Byte, ServerCommand> commands;
    private final ServerCommand ignoreCommand = new IgnoreCommand();

    private CommandProvider() {
        commands = new HashMap<>();

        commands.put((byte) 0, new CloseCommand());
        commands.put((byte) 1, new TimeCommand());
        commands.put((byte) 2, new EchoCommand());

        commands.put((byte) 3, new DownloadCommand());
        commands.put((byte) 4, new UploadCommand());
    }

    public ServerCommand command(byte key) {
        return commands.getOrDefault(key, ignoreCommand);
    }
}
