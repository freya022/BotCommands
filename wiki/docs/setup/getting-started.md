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

## Starting from scratch

If you wish to create your bot differently than what's provided by the bot templates, this is for you, 
you can start by creating a project either using Maven or Gradle.

!!! warning

    1. When creating a Maven project in IntelliJ, do not choose `Maven Archetype` in `Generators`, you must use `New Project`.

    2. The target Java version must be 17+, I recommend using 21.

### Adding the dependencies

The only strictly necessary dependencies are the framework and JDA:

[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?versionPrefix=3&label=BotCommands)](https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest)
[![](https://img.shields.io/maven-central/v/net.dv8tion/JDA?versionPrefix=5&label=JDA)](https://mvnrepository.com/artifact/net.dv8tion/JDA/latest)

=== "Maven"

    ```xml
    <dependencies>
        ...

        <dependency>
            <groupId>net.dv8tion</groupId>
            <artifactId>JDA</artifactId>
            <version>JDA_VERSION</version>
        </dependency>
        <dependency>
            <groupId>io.github.freya022</groupId>
            <artifactId>BotCommands</artifactId>
            <version>BC_VERSION</version>
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

        implementation("net.dv8tion:JDA:JDA_VERSION")
        implementation("io.github.freya022:BotCommands:BC_VERSION")
    }
    ```

### Adding logging

Any SLF4J compatible logger should work; I recommend logback, which you can learn more [here](logging.md).

### Creating the main class

??? "Adding stacktrace-decoroutinator"

    You can optionally add `stacktrace-decoroutinator`, this will help you get clearer stacktrace for code using coroutines.
    
    !!! info
    
        Java users also benefit from it as it may help debug framework issues.
    
    [![](https://img.shields.io/maven-central/v/dev.reformator.stacktracedecoroutinator/stacktrace-decoroutinator-jvm?label=stacktrace-decoroutinator)](https://mvnrepository.com/artifact/dev.reformator.stacktracedecoroutinator/stacktrace-decoroutinator-jvm/latest)
    
    === "Maven"
    
        ```xml
        <dependencies>
            ...
    
            <dependency>
                <groupId>dev.reformator.stacktracedecoroutinator</groupId>
                <artifactId>stacktrace-decoroutinator-jvm</artifactId>
                <version>SD_VERSION</version>
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
    
            implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:SD_VERSION")
        }
        ```

    Finally, load it on the first lines of your main program:

    === "Kotlin"

        ```kotlin
        // stacktrace-decoroutinator has issues when reloading with hotswap agent
        if ("-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments) {
            logger.info { "Skipping stacktrace-decoroutinator as enhanced hotswap is active" }
        } else if ("--no-decoroutinator" in args) {
            logger.info { "Skipping stacktrace-decoroutinator as --no-decoroutinator is specified" }
        } else {
            DecoroutinatorRuntime.load()
        }
        ```

        !!! warning

            `stacktrace-decoroutinator` must be loaded before any coroutine code is loaded, 
            including suspending main functions `suspend fun main() { ... }`.

    === "Java"

        ```java
        // stacktrace-decoroutinator has issues when reloading with hotswap agent
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XX:+AllowEnhancedClassRedefinition")) {
            logger.info("Skipping stacktrace-decoroutinator as enhanced hotswap is active");
        } else if (Arrays.asList(args).contains("--no-decoroutinator")) {
            logger.info("Skipping stacktrace-decoroutinator as --no-decoroutinator is specified");
        } else {
            DecoroutinatorRuntime.INSTANCE.load();
        }
        ```

#### Creating a config service

Create a small `Config` service, it can be a simple object with the properties you need, 
this will be useful when running your bot.

??? example

    === "Kotlin"
        ```kotlin
        class Config(val token: String, val ownerIds: List<Long>) {
            companion object {
                // Makes a service factory out of this property getter
                @get:BService
                val instance by lazy {
                    // Load your config
                }
            }
        }
        ```
    
    === "Java"
        ```java
        public class Config {
            private static Config INSTANCE = null;
        
            private String token;
            private List<Long> ownerIds;
        
            public getToken() { return token; }
            public getOwnerIds() { return ownerIds; }
        
            @BService // Makes this method a service factory that outputs Config objects
            public static Config getInstance() {
                if (INSTANCE == null) {
                    INSTANCE = // Load your config
                }
        
                return INSTANCE;
            }
        }
        ```

!!! info

    You can refer to [the Dependency Injection page](../using-botcommands/dependency-injection.md) for more details

#### Starting the framework

As we've used a singleton pattern for your `Config` class, we can get the same instance anywhere, 
and still be able to get it as a service.

All you need to do is use `BBuilder#newBuilder`:

=== "Kotlin"

    ```kotlin title="Main.kt - Main function"
    val config = Config.instance

    BBuilder.newBuilder {
        addOwners(config.ownerIds)

        // Add the base package of the application
        // All services and commands inside will be loaded
        addSearchPath("io.github.name.bot")

        textCommands {
            usePingAsPrefix = true // The bot will respond to his mention/ping
        }
    }    
    ```

    ??? tip "Using a custom `CoroutineEventManager`"

        I recommend passing a custom `CoroutineEventManager` to `BBuilder#newBuilder`,
        that way you can configure anything such as the amount of threads, their names or their uncaught exception handlers.

        You can use `namedDefaultScope` for this:
        ```kotlin
        // Create a scope for our event manager
        val scope = namedDefaultScope("MyBot Coroutine", 4)
        val manager = CoroutineEventManager(scope, 1.minutes)
        manager.listener<ShutdownEvent> {
            scope.cancel()
        }
        ```

=== "Java"

    ```java title="Main.java - Main method"
    final var config = Config.getInstance();

    BBuilder.newBuilder(builder -> {
        builder.addOwners(config.getOwnerIds());

        // Add the base package of the application
        // All services and commands inside will be loaded
        builder.addSearchPath("io.github.name.bot");

        builder.textCommands(textCommands -> {
            textCommands.usePingAsPrefix(true);
        });
    });
    ```

!!! warning

    JDA must be created **after** the framework is built,
    as the framework listens to JDA events and must not skip any of these.

#### Creating a `JDAService`

Now you've been able to start the framework, all your services should be loaded, 
but you must now have a way to start JDA, implementing `JDAService` will let you start the bot in a convenient place.

=== "Kotlin"

    ```kotlin
    --8<-- "wiki/Bot.kt:jdaservice-kotlin"
    ```

=== "Java"

    ```java
    --8<-- "wiki/java/Bot.java:jdaservice-java"
    ```

!!! info

    Implementing `JDAService` guarantees that your bot will connect at the right time, 
    and provides a way for the framework to check missing intents and missing cache flags before your bot even starts.

    You can retrieve a `JDA` instance once this service has finished constructing.

!!! warning

    Your `JDABuilder` or `DefaultShardManagerBuilder` needs to use the provided event manager,
    this enables the framework to also receive the events as they will both use the same event manager.

You can now run your bot!
Assuming you have done your config class and provided at least the token and owner IDs, 
you should be able to run the help command, by mentioning your bot `@YourBot help`.

### Creating a runnable JAR

=== "Maven"

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.0</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
                <configuration>
                    <transformers>
                        <transformer
                                implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                            <mainClass>io.github.name.bot.Main</mainClass> <!-- TODO change here -->
                        </transformer>
                        <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                    </transformers>
    
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                    <finalName>${artifactId}</finalName>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```

=== "Kotlin Gradle"

    ```kotlin
    plugins {
        ...
        id("com.github.johnrengelman.shadow") version "7.1.2"
    }

    application.mainClass.set("io.github.name.bot.Main")    //TODO change here

    tasks.withType<ShadowJar> {
        archiveFileName.set("your-project-name.jar")        //TODO change here
    }
    ```

While you can run the main class in your IDE during development,
to run your bot in production, you can press ++ctrl++ twice in IntelliJ, then run:

=== "Maven"

    ```
    mvn package
    ```

=== "Kotlin Gradle"

    ```
    gradle shadowJar
    ```

## Other resources

### Examples

You can find examples covering parts of the framework [here](https://github.com/freya022/BotCommands/blob/3.X/examples).

### Getting help

Don't hesitate to join [the support server](https://discord.gg/frpCcQfvTz) if you have any question!