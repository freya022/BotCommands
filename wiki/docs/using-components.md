# Using components

!!! warning "This requires a [database](using-botcommands/database.md) to be set up!"

Components provided by the framework are your usual Discord components with additional features,
they can be configured to:
- Be usable once
- Have timeouts
- Have method handlers or callbacks
- Have constraints (allow list for users/roles/permissions)

To get access to them, you can use the [`Buttons`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components/-buttons/index.html) and [`SelectMenus`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components/-select-menus/index.html) factories,
as well as [`Components`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components/-components/index.html) to delete them manually and make groups.

!!! note "Configuring components with Java"

    When configuring components, you need to use the framework's methods first, 
    and then use the JDA methods, and finally build.

!!! tip "Disabling classes depending on components"

    You can use [`#!java @RequiresComponents`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components.annotations/-requires-components/index.html) if you want your class to be disabled when the components are not available.

## Persistent components
They are components that still work after a restart,
their handlers are methods identified by their handler name, set in [`#!java @JDAButtonListener`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components.annotations/-j-d-a-button-listener/index.html)) / [`#!java @JDASelectMenuListener`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components.annotations/-j-d-a-select-menu-listener/index.html),
when clicked, the button will run that method.

[//]: # (TODO add awaitOrNull KDocs link)

??? tip "Type-safe component methods and optional handlers in Kotlin"

    You can bind a method to your component, enabling you to pass arguments in a type-safe way with `bindTo` extensions.

    You can also not use `bindTo` and instead use `await()` on the built component.

    ```kotlin
    --8<-- "wiki/commands/slash/SlashClickWaiter.kt:click_waiter-kotlin"
    ```

    1. `awaitOrNull` returns `null` when the component expired, useful when combined with an elvis operator,
    this is the equivalent of a `#!java try catch` on `TimeoutCancellationException`.

    2. [`awaitUnit`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.core.utils/await-unit.html) is an extension to await and then return `Unit`, 
    which helps in common scenarios where you want to reply using an elvis operator.

Persistent components have no timeout by default, as their purpose is to be long-lived, however,
you can set one using `timeout`, which accept a timeout handler name, set with [`#!java @ComponentTimeoutHandler`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components.annotations/-component-timeout-handler/index.html).

!!! info

    Components which expired while the bot was offline will run their timeout handlers at startup.

### Example
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashClicker.kt:persistent-clicker-kotlin"
    ```
=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashClickerPersistent.java:persistent-clicker-java"
    ```

## Ephemeral components
They are components which get invalidated after a restart, meaning they can no longer be used,
their handlers are callbacks, which can also have a timeout set, and also use callbacks.

!!! info

    "Invalidated" means that they are deleted from the database, but not necessarily from the message.

Ephemeral components have a default timeout set in [`Components.defaultTimeout`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components/-components/-companion/default-timeout.html), which can be changed.

### Example
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashClicker.kt:ephemeral-clicker-kotlin"
    ```
=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashClickerEphemeral.java:ephemeral-clicker-java"
    ```

## Component groups
Component groups can be created in any component factory, and allow you to configure one timeout for all components.

Also, when one of them gets invalidated (after being used with `oneUse = true`),
the entire group gets invalidated.

For example, this can be useful when the user needs to choose a single button, once.

!!! warning "Ephemeral components in groups"

    If you put ephemeral components in your group, you must disable the timeout with `noTimeout()`.

The timeout works similarly to components, except the annotated handler is a [`#!java @GroupTimeoutHandler`](https://freya022.github.io/BotCommands/docs/-bot-commands/io.github.freya022.botcommands.api.components.annotations/-group-timeout-handler/index.html).

### Example
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashClickGroup.kt:click_group-kotlin"
    ```