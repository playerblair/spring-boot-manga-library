# Manga Library REST API
A simple REST API for cataloguing manga built with Spring Boot.

## Overview
This project is a RESTful API built using Spring Boot that provides endpoints for searching for manga, adding manga to a Mongo database, retrieving stored manga, updating manga information, and updating user progress.

## Prerequisites
- Java 21 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, VS Code, Eclipse, etc.)

## Getting Started
### Clone the repository
```shell
git clone "https://github.com/playerblair/spring-boot-manga-library"
cd spring-boot-manga-library
```
### Build the application
```shell
mvn clean install
```
### Run the application
```shell
mvn spring-boot:run
```
The application will be available at: http://localhost:8080

## API Endpoints
| Method | URL                      | Description                              |
|--------|--------------------------|------------------------------------------|
| GET    | /api/manga               | Get all stored manga.                    |
| GET    | /api/manga/{id}          | Get specific stored manga by ID.         |
| GET    | /api/manga/{id}/progress | Get user progress of specific manga.     |
| GET    | /api/manga/search        | Search external API for manga.           |
| POST   | /api/manga               | Saves manga to database.                 |
| PATCH  | /api/manga/refresh-all   | Refreshes metadata of all stored manga.  |
| PATCH  | /api/manga/{id}/refresh  | Updates metadata of existing manga.      |
| PATCH  | /api/manga/{id}/progress | Updates user progress of existing manga. |
| DELETE | /api/manga/{id}          | Delete a manga.                          |
| POST   | /api/manga/filter        | Filters manga.                           |

## Request & Response Examples
### GET /api/manga
Response:
```json
[
  {
    "malId": 1,
    "title": "Example Manga 1",
    "type": "MANGA",
    "...": "other properties including authors, genres, and user progress"
  },
  {
    "malId": 2,
    "title": "Example Manga 2",
    "type": "ONESHOT",
    "...": "other properties including authors, genres, and user progress"
  }
]
```
### GET /api/manga/1/progress
Response:
```json
{
  "progress": "READING",
  "chaptersRead": 10,
  "volumesRead": 1,
  "rating": 6
}
```
### PATCH /api/manga/1/progress
Request:
```json
{
  "progress": "COMPLETED",
  "chaptersRead": 36,
  "volumesRead": 3,
  "rating": 10
}
```
### POST /api/manga/filter
Response:
```json
{
  "title": "",
  "type": "Manga",
  "status": "Finished",
  "author": "",
  "genres": [],
  "progress": "Reading"
}
```


## Configuration
The application can be configured through the `application.properties` file:
```properties
# Server port
server.port=8080
```

## Running Tests
```shell
mvn tests
```

## Built With
- Spring Boot - The web framework used
- Spring Data Mongo - Data persistence
- Docker Compose - 
- Testcontainers - Testing mongo database
- Maven - Dependency Management