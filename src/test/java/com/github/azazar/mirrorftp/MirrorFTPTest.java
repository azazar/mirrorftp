package com.github.azazar.mirrorftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.ftpserver.FtpServer;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MirrorFTPTest {

    private static FtpServer SERVER;
    private static FTPClient FTP_CLIENT;
    private static Path TEMP_DIR_1;
    private static Path TEMP_DIR_2;
    private static String TEST_BUCKET;
    private static Path BUCKET_DIR_1;
    private static Path BUCKET_DIR_2;
    private static final int PORT = 2121;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";

    @BeforeAll
    public static void setUp() throws Exception {
        Path testTempDir = Files.createTempDirectory("mirrorftptest");

        // Create temporary directories
        TEMP_DIR_1 = testTempDir.resolve("mirrorftptest1");
        TEMP_DIR_2 = testTempDir.resolve("mirrorftptest2");

        Files.createDirectory(TEMP_DIR_1);
        Files.createDirectory(TEMP_DIR_2);

        TEST_BUCKET = "testbucket";

        BUCKET_DIR_1 = TEMP_DIR_1.resolve(TEST_BUCKET);
        BUCKET_DIR_2 = TEMP_DIR_2.resolve(TEST_BUCKET);
        
        Files.createDirectory(BUCKET_DIR_1);
        Files.createDirectory(BUCKET_DIR_2);

        Path file1 = BUCKET_DIR_1.resolve("testfile");
        Path file2 = BUCKET_DIR_2.resolve("testfile");

        Files.createFile(file1);
        Files.createFile(file2);

        // Start the FTP server
        String[] args = {"--user=" + USERNAME, "--password=" + PASSWORD, "--port=" + PORT, TEMP_DIR_1.toString(), TEMP_DIR_2.toString()};
        ServerConfig config = MirrorFTP.parseArguments(args);
        SERVER = MirrorFTP.createServer(config);
        SERVER.start();

        // Connect FTP client
        FTP_CLIENT = new FTPClient();
        FTP_CLIENT.connect("localhost", PORT);
        FTP_CLIENT.login(USERNAME, PASSWORD);
        FTP_CLIENT.enterLocalPassiveMode();

        FTP_CLIENT.changeWorkingDirectory(TEST_BUCKET);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (FTP_CLIENT.isConnected()) {
            FTP_CLIENT.disconnect();
        }
        SERVER.stop();
        // Clean up temporary directories
        FileUtils.deleteDirectory(TEMP_DIR_1.toFile());
        FileUtils.deleteDirectory(TEMP_DIR_2.toFile());
    }

    @Test
    void testListFiles() throws IOException {
        // Create a test file in both directories
        Files.writeString(BUCKET_DIR_1.resolve("testfile.txt"), "Test content");
        Files.writeString(BUCKET_DIR_1.resolve("testfile.txt"), "Test content");

        FTPFile[] files = FTP_CLIENT.listFiles();

        FTPFile file = null;

        for (FTPFile f : files) {
            if (f.getName().equals("testfile.txt")) {
                file = f;
                break;
            }
        }

        assertNotNull(file);
    }

    @Test
    void testUploadFile() throws IOException {
        String content = "Hello, World!";
        boolean result;
        
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            result = FTP_CLIENT.storeFile("uploadtest.txt", inputStream);
        }

        assertTrue(result);
        assertTrue(Files.exists(BUCKET_DIR_1.resolve("uploadtest.txt")));
        assertTrue(Files.exists(BUCKET_DIR_2.resolve("uploadtest.txt")));
        assertEquals(content, Files.readString(BUCKET_DIR_1.resolve("uploadtest.txt")));
        assertEquals(content, Files.readString(BUCKET_DIR_2.resolve("uploadtest.txt")));
    }

    @Test
    void testDownloadFile() throws IOException {
        String content = "Download test content";
        Files.writeString(BUCKET_DIR_1.resolve("downloadtest.txt"), content);
        Files.writeString(BUCKET_DIR_2.resolve("downloadtest.txt"), content);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean result = FTP_CLIENT.retrieveFile("downloadtest.txt", outputStream);
        outputStream.close();

        assertTrue(result);
        assertEquals(content, outputStream.toString());
    }

    @Test
    void testDeleteFile() throws IOException {
        Files.writeString(BUCKET_DIR_1.resolve("deleteme.txt"), "Delete me");
        Files.writeString(BUCKET_DIR_2.resolve("deleteme.txt"), "Delete me");

        boolean result = FTP_CLIENT.deleteFile("deleteme.txt");

        assertTrue(result);
        assertFalse(Files.exists(BUCKET_DIR_1.resolve("deleteme.txt")));
        assertFalse(Files.exists(BUCKET_DIR_2.resolve("deleteme.txt")));
    }

    @Test
    void testCreateDirectory() throws IOException {
        boolean result = FTP_CLIENT.makeDirectory("newdir");

        assertTrue(result);
        assertTrue(Files.isDirectory(BUCKET_DIR_1.resolve("newdir")));
        assertTrue(Files.isDirectory(BUCKET_DIR_2.resolve("newdir")));
    }

    @Test
    void testRemoveDirectory() throws IOException {
        Files.createDirectory(BUCKET_DIR_1.resolve("removeme"));
        Files.createDirectory(BUCKET_DIR_2.resolve("removeme"));

        boolean result = FTP_CLIENT.removeDirectory("removeme");

        assertTrue(result);
        assertFalse(Files.exists(BUCKET_DIR_1.resolve("removeme")));
        assertFalse(Files.exists(BUCKET_DIR_2.resolve("removeme")));
    }

    @Test
    @Disabled // not yet implemented
    void testRenameFile() throws IOException {
        Files.writeString(BUCKET_DIR_1.resolve("oldname.txt"), "Rename me");
        Files.writeString(BUCKET_DIR_2.resolve("oldname.txt"), "Rename me");

        boolean result = FTP_CLIENT.rename("oldname.txt", "newname.txt");

        assertTrue(result);
        assertFalse(Files.exists(BUCKET_DIR_1.resolve("oldname.txt")));
        assertFalse(Files.exists(BUCKET_DIR_2.resolve("oldname.txt")));
        assertTrue(Files.exists(BUCKET_DIR_1.resolve("newname.txt")));
        assertTrue(Files.exists(BUCKET_DIR_2.resolve("newname.txt")));
    }

    @Test
    void testChangeWorkingDirectory() throws IOException {
        try {
            Files.createDirectory(BUCKET_DIR_1.resolve("testdir"));
            Files.createDirectory(BUCKET_DIR_2.resolve("testdir"));

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/"));
            assertEquals("/", FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET));
            assertEquals("/" + TEST_BUCKET, FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/testdir"));
            assertEquals("/" + TEST_BUCKET + "/testdir", FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/../" + TEST_BUCKET));
            assertEquals("/" + TEST_BUCKET, FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/../" + TEST_BUCKET + "/testdir"));
            assertEquals("/" + TEST_BUCKET + "/testdir", FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/testdir/.."));
            assertEquals("/" + TEST_BUCKET, FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/testdir/../.."));
            assertEquals("/", FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET + "/testdir/../../" + TEST_BUCKET));
            assertEquals("/" + TEST_BUCKET, FTP_CLIENT.printWorkingDirectory());

            assertTrue(FTP_CLIENT.changeWorkingDirectory("."));
            assertEquals("/" + TEST_BUCKET, FTP_CLIENT.printWorkingDirectory());
        }
        finally {
            FTP_CLIENT.changeWorkingDirectory("/" + TEST_BUCKET);
        }
    }
}
