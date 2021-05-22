### Building / Installing
#### Downloading the library
You can choose one of the methods:
* Git clone of this repo `git clone https://github.com/freya022/BotCommands.git`
* **OR** download the repository ZIP file, extract it and rename the folder to `BotCommands`

#### Building the library
Once you have it in a folder, change your working directory to it `cd [path_of_your_choice]/BotCommands` <br>
Then you can build the library with `mvn install`, it will build the library and put it in your local Maven dependency folder <br>

#### Using the library
You can now use the library in your Maven projects by adding the dependency like any other Maven dependency.

<details>
<summary>Maven XML - How to add the library</summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.me</groupId>
    <artifactId>TestBot</artifactId>
    <version>1.0-SNAPSHOT</version>

    <build>
        <!-- Possible build properties -->
    </build>
    <repositories>
        <repository> <!-- Repository for JDA -->
            <id>jcenter</id>
            <name>jcenter-bintray</name>
            <url>https://jcenter.bintray.com</url>
        </repository>
    </repositories>
    <dependencies>
        <!-- Your other project's dependencies here -->
        
        <dependency> <!-- Add JDA to your project -->
            <groupId>net.dv8tion</groupId>
            <artifactId>JDA</artifactId>
            <version>4.2.0_229</version>
        </dependency>
        <dependency> <!-- Add BotCommands to your project -->
            <groupId>com.freya02</groupId>
            <artifactId>BotCommands</artifactId>
            <version>1.5.0</version>
        </dependency>
    </dependencies>
</project>
```
</details>