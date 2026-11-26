# Spoonful: CS 157A Final Project

Spoonful is a restaurant reservation website. Customers sign up and book tables, and restaurant
staff register their restaurant and manage it. It is a Spring Boot app with Thymeleaf pages
and a MySQL database accessed through JDBC.

## Requirements

- **Java 25.** Check with `java -version`.
- **MySQL 8.** The MySQL server must be running on `localhost:3306`.
- **Maven is not needed.** The project includes the Maven wrapper (`mvnw` / `mvnw.cmd`).

## 1. Set up the demo database

The demo scripts are in `src/main/java/com/CS157A/FinalProject/database/demo/`:

| File | What it does |
|---|---|
| `schema-demo.sql` | Creates the `SpoonfulDB` database and its tables |
| `sample-data-demo.sql` | Adds demo users and restaurants |

> **Warning:** `schema-demo.sql` drops and recreates the tables, so any data already in them is lost.
> Run it again any time you want to reset the demo data.

Load the schema first, then the sample data. Run these commands from the project folder.

**Option A: MySQL command line.** Open the client, then load each file with `SOURCE`.
This works the same in PowerShell, Command Prompt and macOS/Linux terminals.

```
mysql -u root -p
```

```sql
SOURCE src/main/java/com/CS157A/FinalProject/database/demo/schema-demo.sql;
SOURCE src/main/java/com/CS157A/FinalProject/database/demo/sample-data-demo.sql;
```

**Option B: MySQL Workbench.** Open each file with *File > Open SQL Script*, then run it with the
lightning-bolt button. Run `schema-demo.sql` first.

## 2. Configure the database connection

The app reads its database login from `src/main/resources/db.properties`. That file is ignored
by git, so each person creates their own copy from the example:

```
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

On Windows Command Prompt, use `copy` instead of `cp`, with backslashes in the paths.

Then edit `db.properties` and fill in your MySQL user and password:

```properties
db.url = jdbc:mysql://localhost:3306/SpoonfulDB
db.user = your_mysql_user
db.password = your_mysql_password
```

## 3. Run the application

From the project folder:

```
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run` instead.

When the log shows `Started FinalProjectApplication`, open <http://localhost:8080>.
Stop the app with `Ctrl+C`.

## 4. Try it out

Every demo account uses the password **`password123`**.

| Email | Type | Notes |
|---|---|---|
| `demo@example.com` | Customer | Has a saved special request |
| `maya.patel@example.com` | Customer | No special request |
| `staff@example.com` | Staff | Manages Harbor & Vine |
| `koji.staff@example.com` | Staff | Manages Koji Ramen Bar |

Things to try:

- **Log in** at <http://localhost:8080/login> with a demo account. A wrong password shows
  "Invalid email or password".
- **Sign up a customer** at <http://localhost:8080/register>. Using an email that already exists
  shows "That email is already registered".
- **Register a restaurant** at <http://localhost:8080/register/restaurant>. This creates a staff
  account and its restaurant in one step.
- **Log out** with the button in the navigation bar.

## Troubleshooting

- **`Access denied for user`**: the user or password in `db.properties` is wrong.
- **`Unknown database 'SpoonfulDB'`**: the schema script has not been loaded yet. See step 1.
- **`Table ... doesn't exist`**: load `schema-demo.sql` before `sample-data-demo.sql`.
- **`Port 8080 was already in use`**: another program, or an earlier run of the app, is using
  the port. Stop it, or add `server.port=8081` to `src/main/resources/application.properties`.
