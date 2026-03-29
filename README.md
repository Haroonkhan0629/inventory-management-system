# Inventory Management System

A Java-based inventory management application with both GUI and CLI modes, backed by a MySQL database. The system supports adding, viewing, updating, deleting, and searching inventory items, plus generating a summary report.

## Tools and Technologies Used

- Java 21
- Maven (build and dependency management)
- MySQL 8.x (database)
- JDBC (database connectivity)
- Java Swing (desktop GUI)
- JUnit 5 (testing)
- iTextPDF (reporting dependency)
- Git and GitHub (version control)
- VS Code / IntelliJ IDEA (development environment)

## Key Features

- Dual startup modes:
	- GUI mode (`--gui`)
	- CLI mode (`--cli`)
- Full CRUD operations for inventory items
- Search by item name
- Inventory report generation (totals and inventory value)

## Prerequisites

Install the following before running the project:

- Git
- Java 21 (JDK)
- Maven 3.9+
- MySQL Server 8.x

Verify versions:

```bash
git --version
java -version
mvn -version
mysql --version
```

## Step-by-Step Setup and Usage

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd inventory-management-system
```

If your folder name differs, change into the folder that contains `pom.xml`.

### 2. Create the MySQL Database and Table

Open MySQL (CLI or Workbench) and run:

```sql
CREATE DATABASE IF NOT EXISTS Inventory;
USE Inventory;

CREATE TABLE IF NOT EXISTS items (
		id INT PRIMARY KEY AUTO_INCREMENT,
		name VARCHAR(255) NOT NULL,
		quantity INT NOT NULL,
		price DOUBLE NOT NULL
);
```

### 3. Configure Database Environment Variables

This app reads DB configuration from environment variables:

- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

#### Windows PowerShell (current session)

```powershell
$env:MYSQL_URL="jdbc:mysql://127.0.0.1:3306/Inventory?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_password"
```

If your MySQL root account has no password, use:

```powershell
$env:MYSQL_PASSWORD=""
```

### 4. Build the Project

From the project root (where `pom.xml` is located):

```bash
mvn clean compile
```

### 5. Run the Application

You can run in either GUI or CLI mode.

#### Option A: Run GUI mode

```bash
mvn exec:java -Dexec.mainClass="com.hkhan.app.App" -Dexec.args="--gui"
```

#### Option B: Run CLI mode

```bash
mvn exec:java -Dexec.mainClass="com.hkhan.app.App" -Dexec.args="--cli"
```

If you run without args, the app prompts you to choose GUI or CLI (when a desktop environment is available).

### 6. Use the Program

#### In GUI mode

- Add Item: Fill Name, Quantity, Price, then click **Add Item**
- Update Item: Select a row, edit fields, click **Update Item**
- Delete Item: Select a row, click **Delete Item**
- Search: Enter item name, click **Search**
- Show All: Click **Show All** to reload full inventory
- Generate Report: Click **Generate Report** (outputs report summary to console)

#### In CLI mode

- Choose from the numbered menu:
	1. Add Item
	2. View Items
	3. Update Item
	4. Delete Item
	5. Search Item
	6. Generate Report
	7. Exit

### 7. Run Tests

```bash
mvn test
```

## Troubleshooting

- `MYSQL_URL environment variable not set`
	- Ensure all `MYSQL_*` variables are set in the same terminal session before running.
- Database connection errors
	- Confirm MySQL service is running.
	- Confirm `MYSQL_USER` and `MYSQL_PASSWORD` are correct.
	- Confirm the `Inventory` database and `items` table exist.
- GUI does not launch
	- Use `--cli` mode in headless or remote environments.

## Project Structure

```text
inventory-management-system/
	pom.xml
	src/main/java/com/hkhan/app/
		App.java
		InventoryGUI.java
		dao/ItemDAO.java
		model/Item.java
	src/test/java/com/hkhan/app/
		AppTest.java
```

## Summary

Built a Java Inventory Management System with Swing GUI and CLI interfaces, integrated with MySQL using JDBC and environment-based secure configuration, managed via Maven with unit testing through JUnit 5.
