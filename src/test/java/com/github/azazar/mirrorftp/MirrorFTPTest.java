package com.github.azazar.mirrorftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.ftpserver.FtpServer;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
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

    private static String uploadFile(FTPClient client, String fileName, String content) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            boolean result = client.storeFile(fileName, inputStream);
            assertTrue(result, "Failed to upload file: " + fileName);
        }
        return fileName;
    }

    private static String downloadFile(FTPClient client, String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean result = client.retrieveFile(fileName, outputStream);
        assertTrue(result, "Failed to download file: " + fileName);
        return outputStream.toString();
    }

    private static void deleteFile(FTPClient client, String fileName) throws IOException {
        boolean result = client.deleteFile(fileName);
        assertTrue(result, "Failed to delete file: " + fileName);
    }

    @Test
    void testListFiles() throws IOException {
        String fileName = "testfile.txt";
        String content = "Test content";
        uploadFile(FTP_CLIENT, fileName, content);

        FTPFile[] files = FTP_CLIENT.listFiles();
        FTPFile file = null;
        for (FTPFile f : files) {
            if (f.getName().equals(fileName)) {
                file = f;
                break;
            }
        }

        assertNotNull(file);
        deleteFile(FTP_CLIENT, fileName);
    }

    @Test
    void testUploadFile() throws IOException {
        String fileName = "uploadtest.txt";
        String content = "Hello, World!";
        
        uploadFile(FTP_CLIENT, fileName, content);

        assertTrue(Files.exists(BUCKET_DIR_1.resolve(fileName)));
        assertTrue(Files.exists(BUCKET_DIR_2.resolve(fileName)));
        assertEquals(content, Files.readString(BUCKET_DIR_1.resolve(fileName)));
        assertEquals(content, Files.readString(BUCKET_DIR_2.resolve(fileName)));

        deleteFile(FTP_CLIENT, fileName);
    }

    @Test
    void testDownloadFile() throws IOException {
        String fileName = "downloadtest.txt";
        String content = "Download test content";
        
        uploadFile(FTP_CLIENT, fileName, content);

        String downloadedContent = downloadFile(FTP_CLIENT, fileName);
        assertEquals(content, downloadedContent);

        deleteFile(FTP_CLIENT, fileName);
    }

    @Test
    void testDeleteFile() throws IOException {
        String fileName = "deleteme.txt";
        String content = "Delete me";
        
        uploadFile(FTP_CLIENT, fileName, content);

        deleteFile(FTP_CLIENT, fileName);

        assertFalse(Files.exists(BUCKET_DIR_1.resolve(fileName)));
        assertFalse(Files.exists(BUCKET_DIR_2.resolve(fileName)));
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

    @Test
    void testMultithreadedOperations() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    FTPClient client = new FTPClient();
                    client.connect("localhost", PORT);
                    client.login(USERNAME, PASSWORD);
                    client.enterLocalPassiveMode();
                    client.changeWorkingDirectory("/" + TEST_BUCKET);

                    String fileName = "file_" + threadId + ".txt";
                    String content = "Content from thread " + threadId;

                    uploadFile(client, fileName, content);
                    String downloadedContent = downloadFile(client, fileName);
                    assertEquals(content, downloadedContent);
                    deleteFile(client, fileName);

                    client.disconnect();
                } catch (IOException e) {
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
    }
}
