/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorfs;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class SingleUserManager implements UserManager {

    private final User user;

    public SingleUserManager(User user) {
        this.user = user;
    }

    @Override
    public User getUserByName(String username) throws FtpException {
        if (user.getName().equals(username)) {
            return user;
        }
        return null;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return new String[] { user.getName() };
    }

    @Override
    public void delete(String username) throws FtpException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void save(User user) throws FtpException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean doesExist(String username) throws FtpException {
        return user.getName().equals(username);
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {

        if (authentication instanceof UsernamePasswordAuthentication auth) {
            if (user.getName().equals(auth.getUsername()) && user.getPassword().equals(auth.getPassword())) {
                return user;
            }
        }

        throw new AuthenticationFailedException();
    }

    @Override
    public String getAdminName() throws FtpException {
        return "root";
    }

    @Override
    public boolean isAdmin(String username) throws FtpException {
        return "root".equals(username);
    }
    

}
