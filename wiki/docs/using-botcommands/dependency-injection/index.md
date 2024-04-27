# Dependency injection

Dependency injection provided by this framework is a more lightweight alternative to dedicated frameworks,
quite similarly to [Spring](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html) (which is supported) 
or [CDI using Weld](https://www.baeldung.com/java-ee-cdi).

Rather than you having to construct objects, you may only request them,
the framework will then construct it by providing the dependencies required for your service, wherever they may come from.

This avoids having to pass objects everywhere, allowing a more effective decoupling,
and allows switching implementations in a completely transparent manner.

!!! example

    `ConnectionSupplier` is an interfaced service (an interface that, when implemented, enables the service to be retrieved as such interface).

    You can create an implementation of this interface, per database, enabling you to switch your database, 
    for example, using a configuration file, without changing anything else in your code.

??? info "2.X Migration"

    All singletons / classes with static methods were moved as services, including:

    - `Components`
    - `EventWaiter`
    - `Localization`

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
