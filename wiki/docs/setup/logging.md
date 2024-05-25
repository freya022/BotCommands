In case you are **not** using the bot template, you can add a logger with the following dependencies:

- [slf4j-api](https://mvnrepository.com/artifact/org.slf4j/slf4j-api/latest)
- [logback-classic](https://mvnrepository.com/artifact/ch.qos.logback/logback-classic/latest)

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