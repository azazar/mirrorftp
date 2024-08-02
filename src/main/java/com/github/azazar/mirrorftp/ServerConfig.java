package com.github.azazar.mirrorftp;

import java.io.File;
import java.util.List;

import org.apache.ftpserver.usermanager.impl.BaseUser;

public class ServerConfig {
    private final BaseUser user;
    private final int port;
    private final List<File> storages;

    public ServerConfig(BaseUser user, int port, List<File> storages) {
        this.user = user;
        this.port = port;
        this.storages = storages;
    }

    public BaseUser getUser() {
        return user;
    }

    public int getPort() {
        return port;
    }

    public List<File> getStorages() {
        return storages;
    }
}