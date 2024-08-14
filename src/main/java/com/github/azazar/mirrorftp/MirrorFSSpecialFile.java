/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorftp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class MirrorFSSpecialFile implements FtpFile {

    public static final String FILE_HEALTH = "health.txt";
    public static final String FILE_DF = "df.txt";

    public static final Set<String> SPECIAL_FILES = Set.of(FILE_HEALTH, FILE_DF);

    private MirrorFSRoot root;
    private String name;

    MirrorFSSpecialFile(MirrorFSRoot root, String name) {
        this.root = root;
        this.name = name;
    }

    @Override
    public String getAbsolutePath() {
        return "/" + name;
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
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
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
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getOwnerName() {
        return "nobody";
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
        return 0;
    }

    @Override
    public Object getPhysicalFile() {
        return name;
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
        throw new UnsupportedOperationException("Can't list files in special file");
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        throw new UnsupportedOperationException("Can't create output stream in special file");
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        StringBuilder sb = new StringBuilder();

        switch (name) {
            case FILE_HEALTH -> buildHealthFile(sb);
            case FILE_DF -> buildDfFile(sb);
            default -> throw new FileNotFoundException(name);
        }

        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private MirrorFSFile[] getBuckets() {
        MirrorFSFile[] files = root.listFiles(false)
            .stream()
            .map(f -> ((MirrorFSFile)f))
            .sorted((a, b) -> a.getName().compareTo(b.getName()))
            .toArray(n -> new MirrorFSFile[n]);

        return files;
    }

    public void buildHealthFile(StringBuilder sb) {
        MirrorFSFile[] buckets = getBuckets();

        for (MirrorFSFile bucket : buckets) {
            sb.append(bucket.getName()).append('\n');

            List<String> problems = bucket.getBucketProblems(true);

            if (problems == null || problems.isEmpty()) {
                sb.append("  OK").append('\n');
                continue;
            }

            for (String problem : problems) {
                sb.append("  ").append(problem.trim().replace("\n", "  \n")).append('\n');
            }
        }
    }

    public void buildDfFile(StringBuilder sb) {
        MirrorFSFile[] buckets = getBuckets();

        for (MirrorFSFile bucket : buckets) {
            long free = Long.MAX_VALUE;

            for (File dir : bucket.getStorageFiles()) {
                free = Math.min(dir.getFreeSpace(), free);
            }

            sb.append(bucket.getName()).append('\t').append(FileUtils.byteCountToDisplaySize(free)).append('\n');
        }
    }

}
