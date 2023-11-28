# Writing slash commands

Slash commands are the new way of defining commands, even though there are limitations with them, we do have some advantages such as auto-completion and much easier parsing - things that regular commands can't do reliably

## A few keywords

* `ApplicationCommand` - Must be extended by the class which contains applications commands
* `#!java @AppOption` - Mandatory on options, also allows you to set the option name and description, by default the name is the parameter name (of the method) and description is `No description`
* `#!java @JDASlashCommand` - Annotation for methods which marks slash commands

## Making a slash command

A slash command is similar to a regex prefixed command - You extend `ApplicationCommand` on your class and use `#!java @JDASlashCommand` on every method you want to be a slash command

Your method has to:

* Be public
* Have `GuildSlashEvent` (for guild-only slash commands, if not specified explicitly, a slash command is guild-only) as first parameter, or a `GlobalSlashEvent` for global commands
* Be annotated `#!java @JDASlashCommand`

## Examples

??? note "Basic `/ping` command"

    ```java
    public class SlashPing extends ApplicationCommand {
        @JDASlashCommand(
                scope = CommandScope.GLOBAL,
                name = "ping",
                description = "Pong !"
        )
        public void onPing(GlobalSlashEvent event) {
            event.deferReply().queue();
    
            final long gatewayPing = event.getJDA().getGatewayPing();
            event.getJDA().getRestPing()
                    .queue(l -> event.getHook()
                            .sendMessageFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l)
                            .queue());
        }
    }
    ```

??? note "Example with choices"

    ```java
    public class SlashSay extends ApplicationCommand {
        // If the method is placed in the same file then it is guaranteed to be only the "say" command path,
        // so it won't interfere with other commands
        @Override
        @NotNull
        public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
            if (optionIndex == 0) { //First option
                return List.of(
                        //Only choices here are "Hi" and "Hello" and gets "translated" to their respective values
                        new Command.Choice("Hi", "Greetings, comrad"),
                        new Command.Choice("Hello", "Oy")
                );
            }
    
            return List.of();
        }
    
        @JDASlashCommand(
                //This command is guild-only by default
                name = "say",
                description = "Says what you type"
        )
        public void say(GuildSlashEvent event,
                        //Option name is by default the parameter name
                        @AppOption(description = "What you want to say") String text) {
            event.reply("Your choice: " + text).queue();
        }
    }
    ```
