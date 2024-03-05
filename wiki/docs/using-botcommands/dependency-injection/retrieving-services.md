Any class given by a service provider can be injected into other service providers, 
requesting a service is as simple as declaring a parameter in the class's constructor, 
or the service factory's parameters.

Named services can be retrieved by using `#!java @ServiceName` on the parameter,
this can be omitted if the parameter name matches a service with a compatible type.

!!! tip

    You can also get services manually with `BContext` or `ServiceContainer`, the latter has all methods available, 
    including Kotlin extensions.

!!! example

    === "Java"
        ```java
        @BService // Enables the service to request services and be requested
        public class TagDatabase { /* */ }
        ```

        ```java
        @Command // Enables the command to request services and be requested
        public class TagCommand {
            private final Component components;
            private final TagDatabase tagDatabase;
            
            public TagCommand(
                // You can even request framework services, as long as they are annotated with @BService or @InterfacedService
                Component components,
                // and your own services
                TagDatabase tagDatabase
            ) {
                this.components = components;
                this.tagDatabase = tagDatabase;
            }

            /* */
        }
        ```                

    === "Kotlin"
        ```kotlin
        @BService // Enables the service to request services and be requested
        class TagDatabase { /* */ }
        ```

        ```kotlin
        @Command // Enables the command to request services and be requested
        class TagCommand(
            // You can even request framework services, as long as they are annotated with @BService or @InterfacedService
            // Here I've named it "componentsService" because "components" might conflict with some JDA-KTX builders
            private val componentsService: Components,
            // and your own services
            private val tagDatabase: TagDatabase
        ) {
            /* */
        }
        ```

??? example "Retrieving services by name"

    Consider the following service providers:

    === "Kotlin"
    
        ```kotlin
        --8<-- "wiki/providers/HttpClientProvider.kt:http_client_provider-kotlin"
        ```

        === "With `@ServiceName`"
        
            ```kotlin
            @BService
            class MyApi(@ServiceName("cachedHttpClient") httpClient: HttpClient)
            ```

        === "With parameter names"

            ```kotlin
            @BService
            class MyApi(private val cachedHttpClient: HttpClient)
            ```

    === "Java"

        ```java
        --8<-- "wiki/java/providers/HttpClientProvider.java:http_client_provider-java"
        ```

        === "With `@ServiceName`"
        
            ```java
            @BService
            public class MyApi {
                public MyApi(@ServiceName("cachedHttpClient") HttpClient httpClient) {
                    // ...
                }
            }
            ```

        === "With parameter names"

            ```java
            @BService
            public class MyApi {
                public MyApi(HttpClient cachedHttpClient) {
                    // ...
                }
            }
            ```

            !!! warning

                For this to work, you need to [enable Java parameter names](../parameter-names.md)

### Primary providers

When requesting a service of a specific type/name, there must be at most one *usable* service provider.

For example, if you have two [service factories](creating-services.md#service-factories) with the same return type:

- :x: If both are usable
- :white_check_mark: One has a failing condition, meaning you have one usable provider
- :white_check_mark: One is annotated with `#!java @Primary`, in which case this one is prioritized

!!! note

    You can still retrieve existing services with `ServiceContainer#getInterfacedServices/getInterfacedServiceTypes`

### Interfaced services
A list which the element type is an interfaced service can be requested,
the list will then contain all instantiable instances with the specified type.

!!! example

    `#!java List<ApplicationCommandFilter<?>>` will contain all instances implementing `ApplicationCommandFilter`, 
    which are usable.

### Lazy services
Lazy service retrieval enables you to get lazily-created service, delaying the initialization,
or to get services that are not yet available, such as manually injected services (like `JDA`).

!!! example "Retrieving a lazy service"

    === "Java"
        Request a `Lazy` with the element type being the requested service, 
        and then get the service when needed by using `Lazy#getValue`.

    === "Kotlin"
        Request a `ServiceContainer` and use a delegated property, such as:

        `#!kotlin private val helpCommand: IHelpCommand by serviceContainer.lazy()`

!!! note

    Lazy injections cannot contain a list of interfaced services, 
    nor can a list of lazy services be requested.

### Optional services
When a requested service is not available, and is not a [soft-dependency](creating-services.md#dependencies), 
service creation will fail.

[null-safety]: https://kotlinlang.org/docs/null-safety.html
[default-arguments]: https://kotlinlang.org/docs/functions.html#default-arguments

In case your service does not always require the service,
you can prevent failure by using Kotlin's [nullable][null-safety] / [optional][default-arguments] parameters,
but Java users will need a runtime-retained `#!java @Nullable` annotation 
(such as `#!java @javax.annotation.Nullable`, or, in checker-framework or JSpecify) or `#!java @Optional`.

!!! note "Lazy nullability"

    Lazy services can also have their element type be marked as nullable, 
    for example, `#!java Lazy<@Nullable IHelpCommand>`.