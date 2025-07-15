# 🪟 How to Run OpenMRS Core on Windows

This guide will help you set up and run the OpenMRS Core application on Windows, including SQL database configuration and all necessary dependencies.

## 📋 Prerequisites

Before starting, ensure you have administrator privileges on your Windows machine.

## ☕ Step 1: Install Java Development Kit (JDK)

OpenMRS requires Java 8 or higher.

### Option A: Install OpenJDK (Recommended)
1. 🌐 Visit [Adoptium.net](https://adoptium.net/)
2. 📥 Download OpenJDK 11 or 17 (LTS versions)
3. 🚀 Run the installer and follow the setup wizard
4. ✅ Verify installation:
   ```cmd
   java -version
   javac -version
   ```

### Option B: Install Oracle JDK
1. 🌐 Visit [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/)
2. 📥 Download JDK 11 or 17
3. 🚀 Install and configure JAVA_HOME environment variable

## 🔧 Step 2: Install Apache Maven

Maven is required to build and run OpenMRS.

1. 🌐 Visit [Maven Downloads](https://maven.apache.org/download.cgi)
2. 📥 Download the Binary zip archive (e.g., `apache-maven-3.9.x-bin.zip`)
3. 📂 Extract to `C:\Program Files\Apache\maven`
4. 🔧 Add Maven to PATH:
   - Open System Properties → Advanced → Environment Variables
   - Add `C:\Program Files\Apache\maven\bin` to PATH
5. ✅ Verify installation:
   ```cmd
   mvn -version
   ```

## 🗄️ Step 3: Install MySQL Database

### Option A: MySQL Installer (Recommended)
1. 🌐 Visit [MySQL Downloads](https://dev.mysql.com/downloads/installer/)
2. 📥 Download MySQL Installer for Windows
3. 🚀 Run installer and select "Developer Default"
4. 🔐 Set root password (remember this!)
5. ✅ Complete installation

### Option B: Manual MySQL Installation
1. 📥 Download MySQL Community Server
2. 📂 Extract to `C:\mysql`
3. 🔧 Configure MySQL service
4. 🚀 Start MySQL service

## 💻 Step 4: Configure MySQL via Command Line

### 🔐 Secure MySQL Installation
Open Command Prompt as Administrator:

```cmd
# Connect to MySQL as root
mysql -u root -p

# Create OpenMRS database
CREATE DATABASE openmrs CHARACTER SET utf8 COLLATE utf8_general_ci;

# Create OpenMRS user
CREATE USER 'openmrs'@'localhost' IDENTIFIED BY 'openmrs';

# Grant privileges
GRANT ALL PRIVILEGES ON openmrs.* TO 'openmrs'@'localhost';
FLUSH PRIVILEGES;

# Exit MySQL
EXIT;
```

### 🧪 Test Database Connection
```cmd
mysql -u openmrs -p openmrs
```
Enter password: `openmrs`

## 📁 Step 5: Clone and Setup OpenMRS Core

### 🔄 Clone Repository
```cmd
git clone https://github.com/raimonvibe/openmrs-core-contribution.git
cd openmrs-core-contribution
```

### 🏗️ Build the Project
```cmd
# Clean and build the project
mvn clean package -DskipTests

# This may take 10-15 minutes on first run
```

## 🚀 Step 6: Run OpenMRS

### Method A: Using Jetty (Development)
```cmd
# Navigate to webapp directory
cd webapp

# Start Jetty server
mvn jetty:run
```

### Method B: Using Docker (Alternative)
If you have Docker Desktop installed:
```cmd
# Return to root directory
cd ..

# Start with Docker Compose
docker-compose up
```

## 🌐 Step 7: Access OpenMRS

1. 🌐 Open your web browser
2. 📍 Navigate to: `http://localhost:8080/openmrs`
3. 🎯 Follow the Initial Setup Wizard:
   - **Language**: Select English
   - **Installation Type**: Choose "Simple"
   - **Database**: 
     - MySQL Root Password: (your root password)
     - Database Name: `openmrs`
   - ⏳ Wait for database creation (5-10 minutes)

## 🔧 Troubleshooting

### ❌ Common Issues and Solutions

#### Java Issues
```cmd
# Check JAVA_HOME
echo %JAVA_HOME%

# Set JAVA_HOME if missing
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.x
```

#### Maven Issues
```cmd
# Check Maven installation
mvn --version

# Clear Maven cache if needed
rmdir /s %USERPROFILE%\.m2\repository
```

#### MySQL Connection Issues
```cmd
# Check MySQL service status
net start mysql

# Restart MySQL service
net stop mysql
net start mysql

# Reset MySQL root password if forgotten
mysqld --skip-grant-tables
```

#### Port Conflicts
If port 8080 is in use:
```cmd
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID)
taskkill /PID <process_id> /F
```

### 🔍 Log Files
- **OpenMRS Logs**: `webapp/target/jetty/logs/`
- **MySQL Logs**: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\`

## 📚 Additional Configuration

### 🔧 Runtime Properties
Create `openmrs-runtime.properties` in your user directory:
```properties
# Database connection
connection.url=jdbc:mysql://localhost:3306/openmrs?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8
connection.username=openmrs
connection.password=openmrs

# Application data directory
application_data_directory=C:\\OpenMRS\\

# Auto update database
auto_update_database=true
```

### 🎨 UI Modules
After initial setup, you may want to install UI modules:
1. 🌐 Visit [OpenMRS Modules](https://addons.openmrs.org/)
2. 📥 Download desired modules (.omod files)
3. 📂 Place in `~/.OpenMRS/modules/` directory
4. 🔄 Restart OpenMRS

## 🆘 Getting Help

- 📖 [OpenMRS Documentation](https://wiki.openmrs.org/)
- 💬 [OpenMRS Talk Community](https://talk.openmrs.org/)
- 🐛 [Issue Tracker](https://issues.openmrs.org/)
- 📧 [Developer Mailing List](https://wiki.openmrs.org/x/lwLn)

## 📝 Default Credentials

After setup completion:
- **Username**: `admin`
- **Password**: `Admin123`

## 🔒 Security Notes

⚠️ **Important**: Change default passwords in production environments!

1. 🔐 Change MySQL root password
2. 🔐 Change OpenMRS admin password
3. 🔥 Configure firewall rules
4. 🔒 Use HTTPS in production

## 🎉 Success!

If you see the OpenMRS login page at `http://localhost:8080/openmrs`, congratulations! 🎊 You have successfully set up OpenMRS Core on Windows.

---

**📄 License**: This guide is provided under the same Mozilla Public License 2.0 as the OpenMRS Core project.

**🤝 Contributing**: Found an issue with this guide? Please contribute back to improve it for others!
