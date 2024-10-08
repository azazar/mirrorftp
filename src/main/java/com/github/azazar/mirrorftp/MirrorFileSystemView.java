package com.github.azazar.mirrorftp;

import org.apache.commons.lang3.StringUtils;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

public class MirrorFileSystemView implements FileSystemView {

    FtpFile rootDirectory;
    FtpFile workingDirectory;

    MirrorFileSystemView(FtpFile rootDirectory, FtpFile workingDirectory) {
        this.rootDirectory = rootDirectory;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return rootDirectory;
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return workingDirectory;
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {
        FtpFile newDir = getFile(dir);

        if (newDir != null) {
            if (!newDir.isDirectory()) {
                return false;
            }

            workingDirectory = newDir;

            return true;
        }

        return false;
    }

    @Override
    public FtpFile getFile(String file) throws FtpException {
        while (file.length() > 1 && file.endsWith("/")) {
            file = file.substring(0, file.length() - 1);
        }

        if (file.isEmpty() || ".".equals(file)) {
            return workingDirectory;
        }

        if ("/".equals(file)) {
            return rootDirectory;
        }

        FtpFile baseDirectory;

        if (file.startsWith("/")) {
            baseDirectory = rootDirectory;

            file = file.substring(1);
        }
        else {
            baseDirectory = workingDirectory;
        }

        // String[] parts = ;

        FtpFile ftpFile = baseDirectory;

        for (String part : StringUtils.split(file, '/')) {
            if (".".equals(part)) {
                continue;
            }

            if ("..".equals(part)) {
                if (ftpFile instanceof MirrorFSRoot) {
                }
                else if (ftpFile instanceof MirrorFSFile mirrorFSFile) {
                    ftpFile = mirrorFSFile.parent;
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented method 'getFile(\"" + file + "\")'");
                }

                continue;
            }

            switch (ftpFile) {
                case MirrorFSRoot mirrorFSRoot -> {
                    ftpFile = mirrorFSRoot.getChild(part);
                }
                case MirrorFSFile mirrorFSFile -> ftpFile = new MirrorFSFile(part, mirrorFSFile);
                default -> throw new UnsupportedOperationException("Unimplemented method 'getFile(\"" + file + "\")'");
            }
        }

        return ftpFile;
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {
    }

}
