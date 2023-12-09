# Dependency injection

Dependency injection provided by this framework is a more lightweight alternative to dedicated frameworks, 
quite similarly to [Spring](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html) or [CDI using Weld](https://www.baeldung.com/java-ee-cdi).

Rather than you having to construct objects, you may only request them, 
the framework will then construct it by providing the dependencies required for your service, wherever they may come from.

This avoids having to pass objects everywhere, allowing a more effective decoupling, 
and allows switching implementations in a completely transparent manner.

!!! example

    `ConnectionSupplier` is an interfaced service (an interface that, when implemented, enables the service to be retrieved as such interface).

    You can create an implementation of this interface, per database, enabling you to switch your database, 
    for example, using a configuration file, without changing anything else in your code.

??? tip "2.X Migration"

    If you were using `ExtensionsBuilder#registerConstructorParameter(Class<T>, ConstructorParameterSupplier<T>)` to get objects in commands, 
    it is basically the same, except in a much more complete framework, and without having to declare everything with this method.

    === "2.X"
        === "Java"
            ```java title="TagDatabase.java"
            public class TagDatabase { /* */ }
            ```
    
            ```java title="TagCommand.java"
            public class TagCommand {
                private final TagDatabase tagDatabase;
    
                public TagCommand(TagDatabase tagDatabase) {
                    this.tagDatabase = tagDatabase;
                }
            }
            ```
    
            ```java title="Builder"
            final var tagDatabase = new TagDatabase(/* */);
            
            CommandsBuilder.newBuilder()
                .registerConstructorParameter(TagDatabase.class, clazz -> tagDatabase)
                // Further configuration
                .build();
            ```

        === "Kotlin"
            ```kotlin title="TagDatabase.kt"
            class TagDatabase { /* */ }
            ```
    
            ```kotlin title="TagCommand.kt"
            class TagCommand(private val tagDatabase: TagDatabase) {
                /* */
            }
            ```
    
            ```kotlin title="Builder"
            val tagDatabase = TagDatabase(/* */);
            
            CommandsBuilder.newBuilder()
                .registerConstructorParameter(TagDatabase::class.java) { tagDatabase }
                // Further configuration
                .build();
            ```

    === "3.X"
        === "Java"
            ```java title="TagDatabase.java"
            @BService //Makes this class injectable, can also pull other services in its constructor
            public class TagDatabase { /* */ }
            ```
    
            ```java title="TagCommand.java"
            @Command
            public class TagCommand {
                private final TagDatabase tagDatabase;
    
                public TagCommand(TagDatabase tagDatabase) {
                    this.tagDatabase = tagDatabase;
                }

                /* */
            }
            ```
    
            No specific builder code required!

        === "Kotlin"
            ```kotlin title="TagDatabase.kt"
            @BService //Makes this class injectable, can also pull other services in its constructor
            class TagDatabase { /* */ }
            ```
    
            ```kotlin title="TagCommand.kt"
            @Command
            class TagCommand(private val tagDatabase: TagDatabase) {
                /* */
            }
            ```
    
            No specific builder code required!

## Creating a service
To register a class as a service, add `#!java @BService` to your class declaration.

`#!java @BService` is the base annotation to register a service, 
other annotations exist such as `#!java @Command` and `#!java @Resolver`, 
but the appropriate documentation will specify if such alternatives are required.

!!! info
    
    All classes available for dependency injection must be in the framework's classpath,
    by adding packages to `BBuilder#packages`, or by using `BBuilder#addSearchPath`, 
    all classes are searched recursively.

### Service factories
Service factories are methods that create initialized services themselves,
they accept other services as parameters and define a service with the method's return type.

In addition to the package requirement,
they must be annotated with `#!java @BService`, be in a service, or in an `#!kotlin object`, or be a static method.

??? example

    === "Java"
        ```java
        public class Config {
            private static Config INSTANCE = null;

            /* */

            // Service factory, registers as "Config" (as it is the return type)
            @BService
            public static Config getInstance() {
                if (INSTANCE == null) {
                    // Of course here you would load the config from a file
                    INSTANCE = new Config();
                }
                
                return INSTANCE;
            }
        }
        ```

    === "Kotlin"
        ```kotlin
        class Config {
            /* */

            companion object {
                // Service factory, registers as "Config" (as it is the return type)
                @get:BService
                val instance: Config by lazy {
                    // Of course here you would load the config from a file
                    Config()
                }                
            }
        }
        ```

### Conditional services
Some services may not always be instantiable, 
some may require soft dependencies (prevents instantiation if a service is unavailable, without failing),
while some run a set of conditions to determine if a service can be instantiated.

Services that are not instantiable will not be created at startup, 
will be unavailable for injection and do not figure in the list of interfaced services.

!!! info

    All the following annotations must be used alongside a service-declaring annotation, 
    such as `#!java @BService` or `#!java @Command`.

#### Dependencies
The `#!java @Dependencies` annotation lets you define soft dependencies,
that is, if any of these classes in the annotation are unavailable, your service will not be instantiated.

Without the annotation, any unavailable dependency would throw an exception.

#### Interfaced conditions
`#!java @ConditionalService` defines a list of classes implementing `ConditionalServiceChecker`,
the service is only created if none of these classes return an error message.

`ConditionalServiceChecker` can be implemented on any class that has a no-arg constructor, or is an `#!kotlin object`.

??? example

    === "Java"
        ```java
        --8<-- "wiki/java/commands/slash/TagCommand.java:tag_interfaced_condition-java"
        ```

    === "Kotlin"
        ```kotlin
        --8<-- "wiki/commands/slash/TagCommand.kt:tag_interfaced_condition-kotlin"
        ```

#### Annotation conditions
`#!java @Condition` is a *meta-annotation* (an annotation for annotations) which marks your own annotation as being a condition.

Similar to interfaced conditions, they must refer to an implementation of `CustomConditionChecker`, 
to determine if the annotated service can be created, 
you can also indicate if the service creation must throw an exception in case it fails.

The implementation must have a no-arg constructor, or be an `#!kotlin object`

!!! note
    
    The annotation must also be in the framework's classpath.

??? example

    === "Java"
        ```java title="DevCommand.java"
        --8<-- "wiki/java/switches/DevCommand.java:dev_command_annotated_condition-annotation-java"
        ```

        ```java title="DevCommandChecker.java"
        --8<-- "wiki/java/switches/DevCommandChecker.java:dev_command_annotated_condition-checker-java"
        ```

        ```java title="SlashShutdown.java"
        --8<-- "wiki/java/commands/slash/SlashShutdown.java:dev_command_annotated_condition-command-java"
        ```

    === "Kotlin"
        ```kotlin title="DevCommand.kt"
        --8<-- "wiki/switches/DevCommand.kt:dev_command_annotated_condition-annotation-kotlin"
        
        --8<-- "wiki/switches/DevCommand.kt:dev_command_annotated_condition-checker-kotlin"
        ```

        ```kotlin title="SlashShutdown.kt"
        --8<-- "wiki/commands/slash/SlashShutdown.kt:dev_command_annotated_condition-command-kotlin"
        ```

### Interfaced services

### Service properties

#### Service names

#### Service types

#### Service priority

## Retrieving a service

### Named services

### Interfaced services

### Lazy services

### Optional services