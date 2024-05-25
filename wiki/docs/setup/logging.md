[slf4j-maven-central-shield]: https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fslf4j%2Fslf4j-api%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Bnot(contains(text()%2C%20%22-%22))%5D%5Blast()%5D&logo=apachemaven&label=slf4j-api
[logback-maven-central-shield]: https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fch%2Fqos%2Flogback%2Flogback-classic%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Bnot(contains(text()%2C%20%22-%22))%5D%5Blast()%5D&logo=apachemaven&label=logback-classic

Add a logger with the following dependencies:

![SLF4J version][slf4j-maven-central-shield]
![Logback classic version][logback-maven-central-shield]

=== "Maven"

    ```xml
    <dependencies>
        ...

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>SLF4J_VERSION</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>LOGBACK_VERSION</version>
        </dependency>
    </dependencies>
    ```

=== "Kotlin Gradle"

    ```kotlin
    repositories {
        ...
        mavenCentral()
    }
    
    dependencies {
        ...

        implementation("org.slf4j:slf4j-api:SLF4J_VERSION")
        implementation("ch.qos.logback:logback-classic:LOGBACK_VERSION")
    }
    ```

Here are the logback configs I recommend using:

=== "Dev config"

    I would recommend putting it in your project, in a folder containing your config files, and load it as such:

    !!! danger
        Just like any sensitive data, make sure to add the folder to your .gitignore, 
        as it will contain much more than just the logback config.
        
        However, you can provide a template for contributors/other users.

    === "Kotlin"

        ```kotlin
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.absolutePathString())
        ```
    
    === "Java"
    
        ```java
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.toAbsolutePath().toString());
        ```

    ```xml title="logback-test.xml"
    --8<-- "config-template/logback-test.xml"
    ```

=== "Prod config"

    I would recommend putting it in a folder next to the bot's JAR, and load it as such:

    === "Kotlin"

        ```kotlin
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.absolutePathString())
        ```
    
    === "Java"
    
        ```java
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.toAbsolutePath().toString());
        ```

    ```xml title="logback.xml"
    --8<-- "config-template/logback.xml"
    ```