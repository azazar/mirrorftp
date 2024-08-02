# MirrorFTP

MirrorFTP is a simple FTP server that serves file replicas from the local filesystems of multiple machines. It provides a unified view of files stored across multiple storage locations, ensuring data redundancy and availability.

## Features

- Serves files from multiple storage locations
- Provides a unified view of replicated files
- Implements basic FTP server functionality
- Supports a single user with configurable credentials

## How It Works

MirrorFTP uses a unique approach to serve files from multiple storage locations:

1. **Multiple Storage Locations**: The server accepts two or more storage locations as command-line arguments. These locations are treated as mirrors of each other.

2. **Unified View**: MirrorFTP presents a unified view of the files across all storage locations. If a file exists in any of the storage locations, it will appear in the FTP server's file listing.

3. **Read Operations**: When a client requests to read a file, MirrorFTP randomly selects one of the storage locations that contains the file and serves it from there.

4. **Write Operations**: When a client uploads or modifies a file, MirrorFTP writes the changes to all storage locations simultaneously, ensuring data consistency across all mirrors.

5. **Directory Operations**: Creating or deleting directories affects all storage locations to maintain consistency.

## Key Concepts

1. **MirrorFSRoot**: Represents the root of the file system and manages the list of storage locations.

2. **MirrorFSFile**: Represents a file or directory in the mirrored file system. It keeps track of the file's presence across different storage locations.

3. **MirrorFileOutputStream**: A custom OutputStream that writes data to all storage locations simultaneously when a file is being uploaded or modified.

4. **MirrorFileSystemView**: Provides a unified view of the file system to the FTP server, hiding the complexity of multiple storage locations.

5. **SingleUserManager**: Manages authentication for a single user, as the server is designed for single-user access.

6. **SimpleAuthority**: Provides a simple implementation of FTP server authorities, granting all permissions to the authenticated user.

## Requirements

- Java 21 or higher
- Maven for building the project

## Building the Project

To build the project, run the following command in the project root directory:

```
mvn clean package
```

## Running MirrorFTP

To run MirrorFTP, use the following command:

```
java -jar target/mirrorfs-1.0-SNAPSHOT.jar [options] <storage1> <storage2> [<storage3> ...]
```

Options:
- `-p<password>`: Set the user password (default: changeme)
- `-port=<port>`: Set the listening port (default: 2121)

Example:
```
java -jar target/mirrorfs-1.0-SNAPSHOT.jar -pMySecurePassword -port=2222 /path/to/storage1 /path/to/storage2
```

## Connecting to MirrorFTP

- FTP server runs on port 2121 by default (can be changed using the `-port` option)
- Use the username "user" and the configured password to connect

## Development

The project includes VSCode launch configurations and tasks for easy development and testing. Refer to the `.vscode` directory for more details.

## License

This project is proprietary and confidential. All rights reserved.
