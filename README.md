# MirrorFTP

MirrorFTP is a simple FTP server that serves file replicas from the local filesystems of multiple machines. It provides a unified view of files stored across multiple storage locations, ensuring data redundancy and availability.

## Features

- Serves files from multiple storage locations
- Provides a unified view of replicated files
- Implements basic FTP server functionality
- Supports a single user with configurable credentials

## How It Works

MirrorFTP uses a unique approach to serve files from multiple storage locations:

1. **Multiple Storage Locations**: The server accepts two or more storage locations as command-line arguments. These locations represent different physical storage devices or paths.

2. **Buckets**: Within each storage location, MirrorFTP manages "buckets". A bucket is a top-level directory that contains mirrored content. Buckets with the same name across different storage locations are treated as mirrors of each other.

3. **Unified View**: MirrorFTP presents a unified view of the buckets and their contents across all storage locations. If a file exists in a bucket on any of the storage locations, it will appear in the FTP server's file listing for that bucket.

4. **Read Operations**: When a client requests to read a file from a bucket, MirrorFTP randomly selects one of the storage locations that contains the file in that bucket and serves it from there.

5. **Write Operations**: When a client uploads or modifies a file in a bucket, MirrorFTP writes the changes to all storage locations that contain that bucket simultaneously, ensuring data consistency across all mirrors.

6. **Directory Operations**: Creating or deleting directories within a bucket affects all storage locations that contain that bucket, maintaining consistency across mirrors.

7. **Bucket-Level Mirroring**: Mirroring is done at the bucket level, not the entire storage level. This allows for flexible data distribution and partial mirroring across storage locations.

## Key Concepts

1. **Multiple Storage Locations**: The system manages multiple physical storage locations, allowing for data redundancy and distribution.

2. **Unified File System View**: Despite having multiple storage locations, the system presents a single, unified view of the file system to FTP clients.

3. **Bucket-Level Mirroring**: Files are mirrored at the bucket (top-level directory) level, allowing for flexible data distribution across storage locations.

4. **Distributed Read Operations**: Read requests are distributed across available storage locations, potentially improving read performance.

5. **Synchronized Write Operations**: Write operations are performed simultaneously across all relevant storage locations, ensuring data consistency.

6. **Single-User Authentication**: The system is designed for single-user access, simplifying authentication and authorization.

7. **Custom File System Implementation**: The system implements a custom file system that integrates with Apache FtpServer, allowing for the unique mirroring and distribution features.

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
java -jar target/mirrorftp-1.0-SNAPSHOT.jar [options] <storage1> <storage2> [<storage3> ...]
```

Options:
- `--password=<password>`: Set the user password (default: changeme)
- `--port=<port>`: Set the listening port (default: 2121)

Example:
```
java -jar target/mirrorftp-1.0-SNAPSHOT.jar --password=MySecurePassword --port=2222 /path/to/storage1 /path/to/storage2
```

## Connecting to MirrorFTP

- FTP server runs on port 2121 by default (can be changed using the `--port=` option)
- Use the username "user" and the configured password to connect

## Development

The project includes VSCode launch configurations and tasks for easy development and testing. Refer to the `.vscode` directory for more details.

## License

MirrorFTP is open-source software licensed under the GNU General Public License v3.0 (GPLv3). This license grants you the freedom to use, modify, and distribute the software, provided that any derivative works are also distributed under the same license terms. For the full license text and details on your rights and obligations, please visit the [GNU GPLv3 website](https://www.gnu.org/licenses/gpl-3.0.html).
