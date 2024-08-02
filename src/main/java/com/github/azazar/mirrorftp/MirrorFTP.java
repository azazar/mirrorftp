
package com.github.azazar.mirrorftp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

/**
 * MirrorFTP is a simple FTP server that serves files replicas from the local filesystems of other machines.
 *
 * @author Mikhail Yevchenko <spam@uo1.net>
 */
public class MirrorFTP {

    public static void main(String[] args) throws FtpException {
        ServerConfig config = parseArguments(args);
        FtpServer server = createServer(config);
        server.start();
    }

    public static ServerConfig parseArguments(String[] args) {
        List<File> storages = new ArrayList<>();
        BaseUser user = new BaseUser();
        int port = 2121; // Default port

        user.setAuthorities(List.of(new SimpleAuthority()));
        user.setName("user"); // Default username
        user.setPassword("changeme");

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.startsWith("--user=")) {
                    user.setName(arg.substring("--user=".length()));
                }
                else if (arg.startsWith("--password=")) {
                    user.setPassword(arg.substring("--password=".length()));
                }
                else if (arg.startsWith("--port=")) {
                    port = Integer.parseInt(arg.substring("--port=".length()));
                }
                else if (arg.startsWith("--add-storage-nodes=")) {
                    File dir = new File(arg.substring("--add-storage-nodes=".length()));

                    storages.addAll(Arrays.asList(dir.listFiles(ff -> ff.isDirectory())));
                }
                else {
                    throw new IllegalArgumentException("Unknown option: " + arg);
                }
            }
            else {
                storages.add(new File(arg));
            }
        }
        
        if (storages.size() < 2) {
            throw new IllegalArgumentException("Not enough replicas specified to start MirrorFTP FTP server");
        }

        return new ServerConfig(user, port, storages);
    }

    public static FtpServer createServer(ServerConfig config) throws FtpException {
        FtpServerFactory fsf = new FtpServerFactory();
        
        ListenerFactory lf = new ListenerFactory();
        lf.setPort(config.getPort());
        fsf.addListener("default", lf.createListener());

        fsf.setUserManager(new SingleUserManager(config.getUser()));
        fsf.setFileSystem(new MirrorFileSystemFactory(config.getUser(), config.getStorages().toArray(File[]::new)));
        
        // TODO : configure TLS (see https://mina.apache.org/ftpserver-project/embedding_ftpserver.html for details)

        return fsf.createServer();
    }

}
