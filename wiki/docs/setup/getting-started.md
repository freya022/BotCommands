This tutorial assumes you know how to use your IDE (preferably IntelliJ IDEA), using Maven or Gradle,
and (optionally) how to install PostgreSQL.

## Using the bot template

The framework provides Kotlin and Java templates for a quick start,
while the template uses Maven, it can be substituted with Gradle.

### Getting the template
You can make a repository from the 
([Kotlin](https://github.com/freya022/BotCommands-Template-Kotlin) / [Java](https://github.com/freya022/BotCommands-Template-Java))
template on GitHub, or clone them.

### Preparing your project
You can then configure your build file:
=== "Maven"

    1. In the pom.xml, change the `groupId` to your base package name, such as `io.github.[your username]`, in lowercase.
    2. Replace the `artifactId` and `finalName` (in the `maven-shade-plugin`), with your project's name.

=== "Gradle"

    1. Create a new Gradle project, then copy all files, except `pom.xml`.

    2. Update your build script:

    ```kotlin title="build.gradle.kts"
    import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
    
    plugins {
        java
        application
        id("com.github.johnrengelman.shadow") version "7.1.2"
    }
    
    application.mainClass.set("io.github.[username].bot.Main")    //TODO change here
    group = "io.github.name"                                //TODO change here
    version = "1.0-SNAPSHOT"
    
    tasks.withType<ShadowJar> {
        archiveFileName.set("ProjectName.jar")        //TODO change here
    }
    
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencies {
        //Logging
        implementation("org.slf4j:slf4j-api:[VERSION]") //See https://mvnrepository.com/artifact/org.slf4j/slf4j-api/latest
        implementation("ch.qos.logback:logback-classic:[VERSION]") //See https://mvnrepository.com/artifact/ch.qos.logback/logback-classic/latest

        //JDA
        implementation("net.dv8tion:JDA:[VERSION]") //See https://mvnrepository.com/artifact/net.dv8tion/JDA/latest
        implementation("io.github.freya022:BotCommands:[VERSION]") //See https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest
    }
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    
        //BC supports Java 17 and above
        options.release.set(17)
    }
    ```

You can then update the package of the template, so it matches your group ID.

!!! note

    Don't forget to update the main class in your build script, as well as the main package in the `Main` class.

### Running the bot

!!! warning

    The framework (optionally) uses a database for its components (and so other features depending on components),
    you are required to use PostgreSQL, and so the template is already configured for it,
    including a connection pool (HikariCP), and a database migrator (Flyway).

    If you cannot use PostgreSQL, you can use H2 by:

    1. Replacing the PostgreSQL driver with the [H2 driver](https://github.com/h2database/h2database#downloads) in `pom.xml`
    2. Replacing the JDBC URL in `DatabaseSource` with:
        ```
        jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
        ```
        The URL can be modified to use a file, more details in `ConnectionSupplier`.
    3. Removing the username/password fields

#### Configuring the bot

=== "Dev config"

    1. Copy the `config-template` folder as `dev-config`, 
    and edit the `config.json` with your bot token, prefixes, owner ID and the database details.
    2. Delete `logback.xml`.

    ??? info "How your project folder should look like"

        ```
        IntelliJ-Projects/
        └── ProjectName/
            ├── dev-config/
            │   ├── config.json
            │   └── logback-test.xml
            ├── src/
            │   └── ..
            └── pom.xml/build.gradle.kts
        ```

=== "Prod config"

    1. Copy the `config-template` folder as `config`, 
    and edit the `config.json` with your bot token, prefixes, owner ID and the database details.
    2. Delete `logback-test.xml`.

    ??? info "How your deployed bot's folder should look like"
    
        ```
        ProjectName/
        ├── config/
        │   ├── config.json
        │   └── logback.xml
        └── ProjectName.jar
        ```

#### Making the JAR

During development, run the main class in your IDE.

For your production environment, making a JAR is as simple as opening "Run Anything"
by pressing ++ctrl++ twice in IntelliJ, then running:

=== "Maven"

    ```
    mvn package
    ```

=== "Gradle"

    ```
    gradle shadowJar
    ```

This will generate an executable jar, which you should be running using `#!sh java -jar YourBotName.jar`.

## Other resources

### Examples

You can find examples covering parts of the framework [here](https://github.com/freya022/BotCommands/blob/3.X/examples).

### Getting help

Don't hesitate to join [the support server](https://discord.gg/frpCcQfvTz) if you have any question!