To register a class as a service, add `#!java @BService` to your class declaration.

`#!java @BService` is the base annotation to register a service, 
other annotations exist such as `#!java @Command` and `#!java @Resolver`, 
but the appropriate documentation will specify if such alternatives are required.

!!! info
    
    All classes available for dependency injection must be in the framework's classpath,
    by adding packages to `BConfigBuilder#packages`, or by using `BConfigBuilder#addSearchPath`, 
    all classes are searched recursively.

### Service factories
Service factories are methods that create initialized services themselves,
they accept other services as parameters and define a service with the method's return type.

In addition to the package requirement,
they must be annotated with `#!java @BService`, be in a service, or in an `#!kotlin object`, or be a static method.

!!! note "Terminology"

    Classes registered as services, and service factories, are service providers.

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
                // You can use any method name
                fun createConfig(): Config {
                    // Of course here you would load the config from a file
                    Config()
                }
            }
        }
        ```

    === "Kotlin property"
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
Interfaced services are interfaces, or abstract class, marked by `#!java @InterfacedService`,
they must be implemented by a service.

In addition to the service's type,
implementations of these annotated interfaces have the interface's type automatically added.

Some interfaced services may only be implemented once, some may allow multiple implementations,
if an interfaced service only accepts one implementation, multiple implementations can exist,
but only one must be instantiable.

!!! tip

    You can implement multiple interfaced services at once, 
    which may be useful for text, application and component filters.

??? info "2.X Migration"

    Most methods in `CommandsBuilder` accepting interfaces, implementations or lambdas, were moved to interfaced services:

    **Global:**

    - `CommandsBuilder#setComponentManager`: Removed, using components must be enabled in `BComponentsConfigBuilder#useComponents`, and a `ConnectionSupplier` service be present
    - `CommandsBuilder#setSettingsProvider`: Needs to implement `SettingsProvider`
    - `CommandsBuilder#setUncaughtExceptionHandler`: Needs to implement `GlobalExceptionHandler`
    - `CommandsBuilder#setDefaultEmbedFunction`: Needs to implement `DefaultEmbedSupplier` and `DefaultEmbedFooterIconSupplier`

    **Text commands:**

    - `TextCommandBuilder#addTextFilter`: Needs to implement `TextCommandFilter`, and `TextCommandRejectionHandler`
    - `TextCommandBuilder#setHelpBuilderConsumer`: Needs to implement `HelpBuilderConsumer`

    **Application commands:**

    - `ApplicationCommandBuilder#addApplicationFilter`: Needs to implement `ApplicationCommandFilter`, and `ApplicationCommandRejectionHandler`
    - `ApplicationCommandBuilder#addComponentFilter`: Needs to implement `ComponentCommandFilter`, and `ComponentCommandRejectionHandler`

    **Extensions:**

    - `ExtensionsBuilder#registerAutocompletionTransformer`: Needs to implement `AutocompleteTransformer`
    - `ExtensionsBuilder#registerCommandDependency`: Replaced with standard dependency injection
    - `ExtensionsBuilder#registerConstructorParameter`: Replaced with standard dependency injection
    - `ExtensionsBuilder#registerCustomResolver`: Needs to implement `ClassParameterResolver` and `ICustomResolver`
    - `ExtensionsBuilder#registerDynamicInstanceSupplier`: Needs to implement `DynamicSupplier`
    - `ExtensionsBuilder#registerInstanceSupplier`: Replaced by service factories
    - `ExtensionsBuilder#registerParameterResolver`: Needs to implement `ClassParameterResolver` and the resolver interface of your choices

### Service properties
Service providers can have names, additional registered types, and an instantiation priority.

#### Service names
Named services may be useful if you have multiple services of the same type, but need to get a specific one.

The name is either defined by using `#!java @ServiceName`, or with `BService#name` on the service provider.

!!! example

    You can have a caching `HttpClient` named `cachingHttpClient`, while the usual client uses the default name.

#### Service types
In addition to the type of the service provider, 
`#!java @ServiceType` enables you to register a service as a supertype.

#### Service priority
Service priorities control how service providers are sorted.

A higher priority means that the service will be loaded first,
or that an interfaced service will appear first when [requesting interfaced services](#interfaced-services-1).

The priority is either defined by using `#!java @ServicePriority`, or with `BService#priority` on the service provider, 
see their documentation to learn what how service providers are sorted.