# Starting from scratch

Start by creating a project either using Maven or Gradle, it must run on Java 17+,
I recommend using [Java 21](https://adoptium.net/temurin/releases/?package=jdk&version=21).

!!! warning "Creating a new Maven project"

    When creating a Maven project in IntelliJ, do not choose `Maven Archetype` in `Generators`, you must use `New Project`.

## Adding the dependencies

The only strictly necessary dependencies are the framework and JDA:

[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?versionPrefix=3&label=BotCommands)](https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest)
[![](https://img.shields.io/maven-central/v/net.dv8tion/JDA?versionPrefix=5&label=JDA)](https://mvnrepository.com/artifact/net.dv8tion/JDA/latest)

!!! tip ""

    Omit the `v` prefix from the version, e.g. `5.0.0-beta.18`.

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

## Adding logging

Any SLF4J compatible logger should work; I recommend logback, which you can learn more [here](logging.md).

## Creating a config service

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
        
            public String getToken() { return token; }
            public List<Long> getOwnerIds() { return ownerIds; }
        
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

    You can refer to [the Dependency Injection page](../using-botcommands/dependency-injection/index.md) for more details

## Creating the main class

As we've used a singleton pattern for your `Config` class, we can get the same instance anywhere, 
and still be able to get it as a service.

=== "Built-in DI"

    All you need to do to start the framework is `BotCommands#create`:
    
    === "Kotlin"
    
        ```kotlin title="Main.kt - Main function"
        val config = Config.instance
    
        BotCommands.create {
            addOwners(config.ownerIds)
    
            // Add the base package of the application
            // All services and commands inside will be loaded
            addSearchPath("io.github.name.bot")
    
            textCommands {
                usePingAsPrefix = true // The bot will respond to his mention/ping
            }
        }    
        ```
    
    === "Java"
    
        ```java title="Main.java - Main method"
        final var config = Config.getInstance();
    
        BotCommands.create(builder -> {
            builder.addOwners(config.getOwnerIds());
    
            // Add the base package of the application
            // All services and commands inside will be loaded
            builder.addSearchPath("io.github.name.bot");
    
            builder.textCommands(textCommands -> {
                textCommands.usePingAsPrefix(true);
            });
        });
        ```

=== "Spring IoC"
    
    The framework also supports Spring IoC, 
    in which case you need to add `#!java @EnableBotCommands` on your main application class.

    Configuration of the framework is then done either by using application properties,
    or by implementing configurers, which are explained in the annotation docs.

    Of course, you will need to add component scans to your own classes so it sees commands and other handlers.

??? tip "Kotlin - Using a custom `CoroutineEventManager`"

    I recommend creating a custom `CoroutineEventManager`,
    that way you can configure the amount of threads or their names,
    which may be convenient in logs.

    You can do so by implementing a `ICoroutineEventManagerSupplier` service, 
    with the help of `namedDefaultScope`:
    ```kotlin
    --8<-- "wiki/CoroutineEventManagerSupplier.kt:coroutine_event_manager_supplier-kotlin"
    ```

!!! warning

    JDA must be created **after** the framework is built,
    as the framework listens to JDA events and must not skip any of these,
    you will need to make a service extending `JDAService`.

## Creating a `JDAService`

Now you've been able to start the framework, all your services (such as `Config` for the moment) should be loaded, 
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

    You can retrieve a `JDA` instance once this service has finished constructing, 
    this also implies that you cannot request a `JDA` before `InjectedJDAEvent` has fired.

!!! warning

    Your `JDABuilder` or `DefaultShardManagerBuilder` needs to use the provided event manager,
    this enables the framework to also receive the events as they will both use the same event manager.

You can now run your bot!
Assuming you have done your config class and provided at least the token and owner IDs, 
you should be able to run the help command, by mentioning your bot `@YourBot help`.

## Optional - Add `stacktrace-decoroutinator`

I recommend adding [`stacktrace-decoroutinator`](https://github.com/Anamorphosee/stacktrace-decoroutinator), 
which will help you get clearer stacktrace when using Kotlin coroutines.

!!! note

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

## Creating a runnable JAR

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
        mergeServiceFiles() // Fixes Java's service loading, which is used by Flyway
        archiveFileName.set("your-project-name.jar")        //TODO change here
    }
    ```

While you can run the main class in your IDE during development,
you can create a JAR with all the dependencies by pressing ++ctrl++ twice in IntelliJ, then running:

=== "Maven"

    ```
    mvn package
    ```

=== "Kotlin Gradle"

    ```
    gradle shadowJar
    ```

## Other resources

Take a look at other wiki pages, such as [Dependency injection](../using-botcommands/dependency-injection/index.md), 
[Creating slash command](../using-commands/application-commands/writing-slash-commands.md)
and [Using components](../using-components.md).

### Examples

You can find examples covering parts of the framework [here](https://github.com/freya022/BotCommands/tree/3.X/src/examples).

### Getting help

Don't hesitate to join [the support server](https://discord.gg/frpCcQfvTz) if you have any question!