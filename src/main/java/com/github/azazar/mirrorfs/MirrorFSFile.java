/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class MirrorFSFile implements FtpFile {

    String name;
    FtpFile parent;
    File[] storages;

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
        return false;
	}

	@Override
	public long getSize() {
		if (!isFile()) {
			return 0;
		}

        long size = 0;

		for (File file : storages) {
            size = Math.max(size, file.length());
        }

		return size;
	}

	@Override
	public Object getPhysicalFile() {
        return getAbsolutePath();
	}

	@Override
	public boolean mkdir() {
		throw new UnsupportedOperationException("Unimplemented method 'mkdir'");
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException("Unimplemented method 'delete'");
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
		throw new UnsupportedOperationException("Unimplemented method 'createOutputStream'");
	}

	@Override
	public InputStream createInputStream(long offset) throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'createInputStream'");
	}

	public void addStorage(File storage) {
		storages = Util.arrayAdd(storages, storage);
	}

}
