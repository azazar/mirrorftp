/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorftp;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class SimpleAuthority implements Authority {

    @Override
    public boolean canAuthorize(AuthorizationRequest request) {
        return true;
    }

    @Override
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        return request;
    }

}
