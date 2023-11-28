In case you are **not** using the bot template, you can add a logger with the following dependencies:

- [slf4j-api](https://mvnrepository.com/artifact/org.slf4j/slf4j-api/latest)
- [logback-classic](https://mvnrepository.com/artifact/ch.qos.logback/logback-classic/latest)

You can then create a `logback.xml` file, which you can put in the root of your resources (`src/main/resources`),
or in another place (such as in a config directory), and load them as such:

=== "Kotlin"

    ```kotlin
    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.absolutePathString())
    ```

=== "Java"

    ```java
    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, PATH_TO_LOGBACK.toAbsolutePath().toString());
    ```

Here are the logback configs used in the bot templates:

=== "Dev config"

    ```xml title="logback-test.xml"
    --8<-- "https://github.com/freya022/BotCommands-Template-Kotlin/raw/3.X/config-template/logback-test.xml"
    ```

=== "Prod config"

    ```xml title="logback.xml"
    --8<-- "https://github.com/freya022/BotCommands-Template-Kotlin/raw/3.X/config-template/logback.xml"
    ```