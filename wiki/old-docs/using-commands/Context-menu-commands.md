# Writing context menu commands

Context commands are these commands when you right-click on a message, or on a user, and executes an interaction, similarly to slash commands

## A few keywords

* `[Type]` is either User or Message
* `ApplicationCommand` - Must be extended by the class which contains applications commands
* `@AppOption` - Mandatory on options
* `@JDA[Type]Command` - Annotation for methods which marks context commands

## Making context commands

A context command is similar to a slash command - You extend `ApplicationCommand` on your class and use `@JDA[Type]Command` on every method you want to be a context menu command

Your method has to:
* Be public
* Have `Guild[Type]Event` (for guild-only context commands, if not specified explicitly, a context command is guild-only) as first parameter, or a `Global[Type]Event` for global commands
* Be annotated `@JDA[Type]Command`

## Examples
??? note "Basic `Quote message` message command"

    ```java
    public class ContextQuote extends ApplicationCommand {
        @JDAMessageCommand(name = "Quote message")
        public void execute(GuildMessageEvent event) {
            final Message targetMessage = event.getTargetMessage();
    
            event.reply("> " + targetMessage.getContentRaw()).queue();
        }
    }
    ```

??? note "Basic `Get avatar user` command"

    ```java
    public class ContextAvatar extends ApplicationCommand {
        @JDAUserCommand(name = "Get avatar")
        public void execute(GuildUserEvent event) {
            final User targetUser = event.getTargetUser();
    
            event.reply(targetUser.getEffectiveAvatarUrl()).queue();
        }
    }
    ```

## Updating existing context commands on the fly

See the [Updating existing commands page](./using-slash-commands/Updating-slash-commands.md)