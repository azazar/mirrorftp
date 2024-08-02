/*
 * PROPRIETARY/CONFIDENTIAL
 */

package com.github.azazar.mirrorfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

/**
 * MirrorFS is a simple FTP server that serves files replicas from the local filesystems of other machines.
 *
 * @author Mikhail Yevchenko <spam@uo1.net>
 */
public class MirrorFS {

    public static void main(String[] args) throws FtpException {
        List<File> storages = new ArrayList<>();
        
        BaseUser user = new BaseUser();
        int port = 2121; // Default port

        user.setAuthorities(List.of(new SimpleAuthority()));
        user.setName("user");
        user.setPassword("changeme");

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.startsWith("-p")) {
                    user.setPassword(arg.substring(2));
                }
                else if (arg.startsWith("-port=")) {
                    port = Integer.parseInt(arg.substring(6));
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
            throw new IllegalArgumentException("Not enough replicas specified to start MirrorFS FTP server");
        }

        FtpServerFactory fsf = new FtpServerFactory();
        
        ListenerFactory lf = new ListenerFactory();

        lf.setPort(port);
        
        fsf.addListener("default", lf.createListener());

        fsf.setUserManager(new SingleUserManager(user));

        fsf.setFileSystem(new MirrorFileSystemFactory(user, storages.toArray(File[]::new)));
        
        // TODO : configure TLS (see https://mina.apache.org/ftpserver-project/embedding_ftpserver.html for details)

        fsf.createServer().start();
    }

}
