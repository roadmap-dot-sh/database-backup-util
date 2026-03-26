# Database Backup Utility

Build a database backup utility that can backup and restore any DB

Project
URL: <a href="https://roadmap.sh/projects/database-backup-utility">https://roadmap.sh/projects/database-backup-utility</a>

---

You are required to build a command-line interface (CLI) utility for backing up any type of database. The utility will
support various database management systems (DBMS) such as MySQL, PostgreSQL, MongoDB, SQLite, and others. The tool will
feature automatic backup scheduling, compression of backup files, storage options (local and cloud), and logging of
backup activities.

## Project Requirements

The CLI tool should support the following features:

### Database Connectivity

- <b>Support for Multiple DBMS</b>: Provide support for connecting to various types of databases (e.g., MySQL,
  PostgreSQL,
  MongoDB).
- <b>Connection Parameters</b>: Allow users to specify database connection parameters. Parameters may include host,
  port,
  username, password, and database name.
- <b>Connection Testing</b>: Validate credentials based on the database type before proceeding with backup operations.
- <b>Error Handling</b>: Implement error handling for database connection failures.

### Backup Operations

- <b>Backup Types</b>: Support full, incremental, and differential backup types based on the database type and user
  preference.
- <b>Compression</b>: Compress backup files to reduce storage space.

### Storage Options

- <b>Local Storage</b>: Allow users to store backup files locally on the system.
- <b>Cloud Storage</b>: Provide options to store backup files on cloud storage services like AWS S3, Google Cloud
  Storage, or
  Azure Blob Storage.

### Logging and Notifications

- <b>Logging</b>: Log backup activities, including start time, end time, status, time taken, and any errors encountered.
- <b>Notifications</b>: Optionally send slack notification on completion of backup operations.

### Restore Operations

- <b>Restore Backup</b>: Implement a restore operation to recover the database from a backup file.
- <b>Selective Restore</b>: Provide options for selective restoration of specific tables or collections if supported by
  the
  DBMS.

## Constraints

Feel free to use any programming language or framework of your choice to implement the database backup utility. Ensure
that the tool is well-documented and easy to use. You can leverage existing libraries or tools for database connectivity
and backup operations.

- The tool should be designed to handle large databases efficiently.
- Ensure that the backup and restore operations are secure and reliable.
- The utility should be user-friendly and provide clear instructions for usage (e.g., help command).
- Consider the performance implications of backup operations on the database server.
- Implement proper error handling and logging mechanisms to track backup activities.
- Ensure compatibility with different operating systems (Windows, Linux, macOS).

## Run Application

### 1. Clone repository

```shell
git clone https://github.com/roadmap-dot-sh/database-backup-util.git
cd database-backup-util
```

### 2. Build the tool

```shell
mvn clean package
```

### 3. Run and test

#### - Perform a backup

```shell
java -jar target/database-backup-util-1.0-SNAPSHOT.jar backup \
  --db-type mysql \
  --host localhost \
  --port 3306 \
  --username root \
  --database mydb \
  --backup-type full \
  --compress true \
  --storage local \
  --storage-path /backups \
  --slack-webhook https://hooks.slack.com/services/xxx
```

#### - Restore a backup

```shell
java -jar target/database-backup-util-1.0-SNAPSHOT.jar restore \
  --db-type mysql \
  --host localhost \
  --port 3306 \
  --username root \
  --database mydb \
  --backup-file /backups/mydb_20240101_120000.sql.gz \
  --tables users,orders
```

#### - Schedule backups

```shell
java -jar target/database-backup-util-1.0-SNAPSHOT.jar schedule \
  --cron "0 0 2 * * ?" \
  --config-file backup-config.json
```

#### - Test connect

```shell
java -jar target/database-backup-util-1.0-SNAPSHOT.jar test-connection \
  --db-type postgresql \
  --host localhost \
  --port 5432 \
  --username postgres \
  --database mydb
```