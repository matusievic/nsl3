package nc.server.command.impl;

import nc.server.command.ServerCommand;
import nc.util.BitOps;
import nc.util.PacketConf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class UploadCommand implements ServerCommand {
    private String outputFileName;

    @Override
    public void execute(DatagramSocket server, SocketAddress address, List<byte[]> datagrams) throws IOException {
        byte[] init = datagrams.remove(0);
        short count = BitOps.byteToShort(Arrays.copyOfRange(init, PacketConf.countOffset, PacketConf.countOffset + 2));
        outputFileName = new String(Arrays.copyOfRange(init, PacketConf.payloadOffset, PacketConf.payloadOffset + count));
        System.out.println(address + " > UPLOAD " + outputFileName);
        Files.createFile(Paths.get("lab-3/server" + File.separator + outputFileName));

        FileOutputStream output = new FileOutputStream("lab-3/server" + File.separator + outputFileName, true);
        for (byte[] d : datagrams) {
            count = BitOps.byteToShort(Arrays.copyOfRange(d, PacketConf.countOffset, PacketConf.countOffset + 2));
            output.write(Arrays.copyOfRange(d, PacketConf.payloadOffset, PacketConf.payloadOffset + count));
        }

        System.out.println(address + " > UPLOADING FINISHED");
    }
}
