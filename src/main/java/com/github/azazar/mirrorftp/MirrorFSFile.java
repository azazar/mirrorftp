package com.github.azazar.mirrorftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.apache.ftpserver.ftplet.FtpFile;

public class MirrorFSFile implements FtpFile {

    private static final Random RNG = new Random();

    String name;
    FtpFile parent;
    File[] storages;
    private File[] storageFiles = null;

    MirrorFSFile(String name, MirrorFSRoot root) {
        this.name = name;
        this.parent = root;
        this.storages = null;
    }

    MirrorFSFile(String name, MirrorFSFile parent) {
        this.name = name;
        this.parent = parent;
        this.storages = parent.storages;
    }

    @Override
    public String getAbsolutePath() {
        String parentPath = parent.getAbsolutePath();

        if (parentPath.endsWith("/")) {
            return parentPath + name;
        }
        
        return parentPath + "/" + name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        if (parent instanceof MirrorFSRoot)
            return true;
        
        for (File path : getStorageFiles()) {
            if (!path.isDirectory()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean isFile() {
        if (parent instanceof MirrorFSRoot)
            return false;

        for (File path : getStorageFiles()) {
            if (!path.isFile()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean doesExist() {
        for (File path : getStorageFiles()) {
            if (path.exists()) {
                return true;
            }
        }
        
        return false;
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
        if (parent instanceof MirrorFSRoot)
            return false;

        return isWritable();
    }

    @Override
    public String getOwnerName() {
        return parent.getOwnerName();
    }

    @Override
    public String getGroupName() {
        return parent.getGroupName();
    }

    @Override
    public int getLinkCount() {
        return 1;
    }

    @Override
    public long getLastModified() {
        long lastModified = 0;
        for (File file : storages) {
            if (file.lastModified() > lastModified) {
                lastModified = file.lastModified();
            }
        }
        return lastModified;
    }

    @Override
    public boolean setLastModified(long time) {
        for (File path : getStorageFiles()) {
            if (!path.setLastModified(time)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getSize() {
        if (!isFile()) {
            return 0;
        }

        long size = 0;

        for (File file : getStorageFiles()) {
            size = Math.max(size, file.length());
        }

        return size;
    }

    @Override
    public Object getPhysicalFile() {
        return getAbsolutePath();
    }

    public File[] getStorageFiles() {
        if (storageFiles == null) {
            storageFiles = new File[storages.length];

            for (int i = 0; i < storages.length; i++) {
                storageFiles[i] = new File(storages[i], getAbsolutePath().substring(1));
            }
        }

        return storageFiles;
    }

    @Override
    public boolean mkdir() {
        boolean result = true;

        for (File f : getStorageFiles()) {
            result = result && f.mkdir();
        }

        return result;
    }

    @Override
    public boolean delete() {
        boolean result = true;

        for (File f : getStorageFiles()) {
            result = result && f.delete();
        }

        return result;
    }

    @Override
    public boolean move(FtpFile destination) {
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        LinkedHashMap<String, FtpFile> files = new LinkedHashMap<>();

        for (File storage : storages) {
            for(File child : new File(storage, getAbsolutePath().substring(1)).listFiles()) {
                files.computeIfAbsent(getAbsolutePath() + "/" + child.getName(), path -> new MirrorFSFile(child.getName(), this));
            }
        }

        return new ArrayList<>(files.values());
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        if (parent instanceof MirrorFSRoot)
            throw new IOException("Can't create output stream for root directory");

        return new MirrorFileOutputStream(getStorageFiles(), offset);
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        if (parent instanceof MirrorFSRoot)
            throw new IOException("Can't create input stream for root directory");

        return new FileInputStream(getStorageFiles()[RNG.nextInt(storages.length)]);
    }

    public void addStorage(File storage) {
        storages = Util.arrayAdd(storages, storage);
    }

}
