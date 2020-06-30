# Database

This project uses MySQL DBMS. You will need two things: a running database with the table `users` (see the SQL dump) and a db.config file.

## SQL dump
Create a utf8mb4_general_ci database and run the SQL dump found in resources/users-table-sql-dump.sql.

## db.config
Create a db.config file and place in the resources/ folder of the project. Make sure the file has reading rights.
The db.config file should contain the following information:

```
host=localhost
port=3306
database=DB_NAME
username=DB_USERNAME
password=DB_PASSWORD
```