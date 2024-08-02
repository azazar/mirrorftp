/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class MirrorFSRoot implements FtpFile {

    private final MirrorFileSystemFactory fs;
    private final File[] storages;

    MirrorFSRoot(MirrorFileSystemFactory fs, File[] storages) {
        this.fs = fs;
        this.storages = storages;
    }

    @Override
    public String getAbsolutePath() {
        return "/";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean doesExist() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getOwnerName() {
        return fs.fsUser.getName();
    }

    @Override
    public String getGroupName() {
        return "nobody";
    }

    @Override
    public int getLinkCount() {
        return 1;
    }

    @Override
    public long getLastModified() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean setLastModified(long time) {
        return false;
    }

    @Override
    public long getSize() {
        return storages.length;
    }

    @Override
    public Object getPhysicalFile() {
        return "/";
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile destination) {
        return false;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        HashMap<String, MirrorFSFile> files = new HashMap<>();

        for (File storage : storages) {
            for(File child : storage.listFiles()) {
                if (child.isDirectory()) {
                    files.computeIfAbsent(child.getName(), name -> new MirrorFSFile(name, this)).addStorage(storage);
                }
            }
        }

        return new ArrayList<>(files.values());
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'createOutputStream'");
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'createInputStream'");
    }

    public void addStoragesTo(MirrorFSFile ftpFile) {
        for (File storage : storages) {
            if (new File(storage, ftpFile.name).isDirectory()) {
                ftpFile.addStorage(storage);
            }
        }
    }

}
