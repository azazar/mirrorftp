/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorfs;

import java.io.File;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class MirrorFileSystemFactory implements FileSystemFactory {

    final User fsUser;

    private final MirrorFSRoot root;

    public MirrorFileSystemFactory(User fsUser, File[] storages) {
        this.fsUser = fsUser;
        if (storages == null || storages.length < 2) {
            throw new IllegalArgumentException("At least two storages are required");
        }

        this.root = new MirrorFSRoot(this, storages);
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {
        if (!fsUser.getName().equals(user.getName())) {
            throw new FtpException("User " + user.getName() + " is not " + fsUser);
        }

        return new MirrorFileSystemView(root, root);
    }

}
