package com.github.azazar.mirrorftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.ftpserver.ftplet.FtpFile;

public class MirrorFSFile implements FtpFile {

    private static final Random RNG = new Random();
    private static final String PROPS_FILENAME = "mirrorftp.properties";

    String name;
    FtpFile parent;
    File[] storages;
    private File[] storageFiles = null;

    MirrorFSFile(String name, MirrorFSRoot root) {
        this.name = name;
        this.parent = root;
        this.storages = null;

        if (PROPS_FILENAME.equals(name)) {
            throw new IllegalArgumentException("File name '" + name + "' is reserved");
        }
    }

    MirrorFSFile(String name, MirrorFSFile parent) {
        this.name = name;
        this.parent = parent;
        this.storages = parent.storages;

        if (PROPS_FILENAME.equals(name)) {
            throw new IllegalArgumentException("File name '" + name + "' is reserved");
        }
    }

    @Override
    public String getAbsolutePath() {
        String parentPath = parent.getAbsolutePath();

        if (parentPath.endsWith("/")) {
            return parentPath + name;
        }
        
        return parentPath + "/" + name;
    }

    private MirrorFSFile getBucketHome() {
        FtpFile file = this;

        while (file instanceof MirrorFSFile mirrorFSFile) {
            if (mirrorFSFile.parent instanceof MirrorFSRoot) {
                return mirrorFSFile;
            }

            file = mirrorFSFile.parent;
        }
        
        return null;
    }

    boolean isBucketHome() {
        return parent instanceof MirrorFSRoot;
    }

    public boolean isBucketHealthy() {
        List<String> problems = getBucketProblems(false);

        return getBucketProblems(false) == null || problems.isEmpty();
    }

    public List<String> getBucketProblems(boolean findAny) {
            MirrorFSFile bucketHome = getBucketHome();

        Properties props = null;
        int foundStorages = 0;

        ArrayList<String> problems = new ArrayList<>();

        for(File storage : bucketHome.getStorageFiles()) {
            File propsFile = new File(storage, PROPS_FILENAME);

            if (propsFile.exists()) {
                foundStorages++;

                if (props == null) {
                    try(FileInputStream in = new FileInputStream(propsFile)) {
                        props = new Properties();

                        props.load(in);
                    } catch (IOException ex) {
                        problems.add("Can't read replica " + storage + " properties: " + ex.getMessage());

                        if (findAny) {
                            return problems;
                        }
                    }
                }
            }
        }

        if (!problems.isEmpty()) {
            return problems;
        }

        if (foundStorages == 0) {
            for(File storage : bucketHome.getStorageFiles()) {
                File propsFile = new File(storage, PROPS_FILENAME);

                props = new Properties();

                props.setProperty("replicas", Integer.toString(storages.length));

                try(FileOutputStream out = new FileOutputStream(propsFile)) {
                    props.store(out, "# MirrorFS Replica Properties");
                } catch (IOException ex) {
                    problems.add("Can't write replica " + storage + " properties: " + ex.getMessage());

                    if (findAny) {
                        return problems;
                    }
            }
            }

            return problems;
        }

        if (props == null) {
            problems.add("Can't read replica properties");

            return problems;
        }

        if (foundStorages != storages.length) {
            problems.add("Wrong number of replicas (found: " + storages.length + ", expected: " + foundStorages + ")");
        }

        if (storages.length != Integer.parseInt(props.getProperty("replicas", "-1"))) {
            problems.add("Wrong number of replicas (found: " + storages.length + ", expected: " + props.getProperty("replicas") + ")");
        }

        return problems;
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
        if (!isBucketHealthy()) {
            return false;
        }

        boolean result = true;

        for (File f : getStorageFiles()) {
            result = result && f.mkdir();
        }

        return result;
    }

    @Override
    public boolean delete() {
        if (!isBucketHealthy()) {
            return false;
        }

        boolean result = true;

        for (File f : getStorageFiles()) {
            result = result && f.delete();
        }

        return result;
    }

    @Override
    public boolean move(FtpFile destination) {
        if (!isBucketHealthy()) {
            return false;
        }

        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        LinkedHashMap<String, FtpFile> files = new LinkedHashMap<>();

        for (File storage : storages) {
            for(File child : new File(storage, getAbsolutePath().substring(1)).listFiles()) {
                String filename = child.getName();

                if (PROPS_FILENAME.equals(filename) && isBucketHome()) {
                    continue;
                }

                files.computeIfAbsent(getAbsolutePath() + "/" + filename, path -> new MirrorFSFile(filename, this));
            }
        }

        return new ArrayList<>(files.values());
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        if (!isBucketHealthy()) {
            throw new IOException("Bucket is not in healthy state");
        }

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
