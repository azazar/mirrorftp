/*
 * PROPRIETARY/CONFIDENTIAL
 */

package com.github.azazar.mirrorfs;

import java.io.File;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

/**
 *
 * @author m
 */
public class MirrorFS {

    public static void main(String[] args) throws FtpException {
        FtpServerFactory fsf = new FtpServerFactory();
        
        ListenerFactory lf = new ListenerFactory();

        lf.setPort(2121);
        
        fsf.addListener("default", lf.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File("users.properties"));
        fsf.setUserManager(userManagerFactory.createUserManager());
        
        fsf.createServer().start();

        // TODO : configure TLS (see https://mina.apache.org/ftpserver-project/embedding_ftpserver.html for details)
    }
}
