# Common command details
All commands can be defined by either using annotations or a DSL (for Kotlin users).

In both cases, classes containing commands need to be annotated with `#!java @Command`, 
which will also register your class for dependency injection.

## Defining the command method
Whether you're using annotated or DSL commands, you will have to write a method,
which holds the user inputs as parameters.

The method must be public, non-static, with the first parameter being the event required for the command type of your choice.

The command methods support coroutines, as well as nullable options, and optionals.

??? example "A slash command with everything mentioned above"
    === "Kotlin"
        ```kotlin
        suspend fun example(event: GuildSlashEvent, string: String, user: User?, integer: Int = 42/*(1)!*/) {}    
        ```

        1. Optional parameters make the Discord option optional too.

    === "Java"
        ```java
        public void example(@NotNull GuildSlashEvent event, @NotNull String string, @Nullable User user) {}
        ```

## Annotated commands
Annotations let you create commands easily, but are harder to read, cannot be created dynamically,
and require usage of other methods to retrieve other values (such as choices for slash command options).

Requirements of such commands are specified on the command annotation.

## DSL commands (Kotlin)
DSL commands were added in V3 to help create commands dynamically,
whether it's to let the user filter commands themselves, or adding subcommands/options in a loop;
you can almost do anything you want while keeping the simplicity of your command method.

Commands can be added by declaring a method, which will declare your commands when called,
the method parameters and its annotations will depend on which commands you want to declare.