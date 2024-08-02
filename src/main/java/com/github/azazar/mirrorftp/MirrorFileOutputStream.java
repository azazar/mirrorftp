/*
 * PROPRIETARY/CONFIDENTIAL
 */
package com.github.azazar.mirrorftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Yevchenko <m.ṥῥẚɱ.ѓѐḿởύḙ@uo1.net>
 */
public class MirrorFileOutputStream extends OutputStream {

    private final OutputStream[] streams;

    public MirrorFileOutputStream(File[] files) throws FileNotFoundException {
        this.streams = new OutputStream[files.length];
        for (int i = 0; i < files.length; i++) {
            this.streams[i] = new FileOutputStream(files[i]);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b);
        }
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        Throwable exception = null;

        for (OutputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException | RuntimeException ex) {
                if (exception == null)
                    exception = ex;
                else
                    exception.addSuppressed(ex);
            }
        }

        if (exception != null) {
            if (exception instanceof IOException ex) {
                throw ex;
            }
            
            if (exception instanceof RuntimeException ex) {
                throw ex;
            }
        }
    }

}
