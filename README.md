# MirrorFS

MirrorFS is a simple FTP server that serves file replicas from the local filesystems of multiple machines. It provides a unified view of files stored across multiple storage locations, ensuring data redundancy and availability.

## Features

- Serves files from multiple storage locations
- Provides a unified view of replicated files
- Implements basic FTP server functionality
- Supports a single user with configurable credentials

## Requirements

- Java 21 or higher
- Maven for building the project

## Building the Project

To build the project, run the following command in the project root directory:

```
mvn clean package
```

## Running MirrorFS

To run MirrorFS, use the following command:

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

## Connecting to MirrorFS

- FTP server runs on port 2121 by default (can be changed using the `-port` option)
- Use the username "user" and the configured password to connect

## Development

The project includes VSCode launch configurations and tasks for easy development and testing. Refer to the `.vscode` directory for more details.

## License

This project is proprietary and confidential. All rights reserved.
