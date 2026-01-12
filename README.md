# Book Search System - CS370 Phase 3

A Java application for searching books online, storing results in hash-based storage, and performing offline queries with user authentication and transaction logging.

## Features

- **Online Search**: Search books on OpenLibrary.org and scrape results
- **Offline Queries**: Query stored data by time range or title keyword
- **User Authentication**: Login, registration, and role-based access (ADMIN, USER, GUEST)
- **Transaction Logging**: All data operations are logged for persistence
- **Favorites & Notes**: Users can mark favorites and add personal notes (Innovation #1)
- **Analytics Dashboard**: Admin-only analytics from transaction log (Innovation #2)
- **Storage Rebuild**: Admin can rebuild storage from transaction log

## Requirements

- Java JDK 8 or higher
- Internet connection (for online search)

## Compilation

```bash
# Compile all Java files
javac -d bin -encoding UTF-8 src/model/*.java src/storage/*.java src/auth/*.java src/log/*.java src/web/*.java src/service/*.java src/cli/*.java src/gui/*.java src/Main.java
```

Or on Windows PowerShell:
```powershell
javac -d bin -encoding UTF-8 src/model/*.java src/storage/*.java src/auth/*.java src/log/*.java src/web/*.java src/service/*.java src/cli/*.java src/gui/*.java src/Main.java
```

## Running

### Start with empty storage:
```bash
java -cp bin Main --startEmpty
```

### Start with initial data:
```bash
java -cp bin Main --loadInitialData initial_data.txt
```

### Default login:
- **Username**: `admin`
- **Password**: `admin`

## File Formats

### users.txt
Format: `username|passwordHash|role`
- Default admin account is created automatically if file doesn't exist

### transactions.log
Format: `TIMESTAMP | USERNAME | ACTION | ID | TITLE | ADDITIONAL_FIELDS_JSON`
- Append-only log file for all data operations

### initial_data.txt
Same format as transactions.log, but only INSERT actions are processed during initial load.

## User Roles

- **ADMIN**: Full access including log viewing, storage rebuild, and analytics
- **USER**: Can search, store data, perform offline queries, and use favorites/notes
- **GUEST**: Can search online but cannot store results

## Notes

- HTML parsing uses basic string operations (no third-party libraries)
- Storage uses hash-based HashMap implementation
- All data mutations are logged to transactions.log
- Admin can rebuild storage from log if needed

