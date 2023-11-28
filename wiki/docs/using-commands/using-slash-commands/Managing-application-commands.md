# Adding or removing *application* commands from certain Guilds

You can specify which application commands are available on a per-guild basis by using a `SettingsProvider`

You do need to set the `SettingsProvider` with `CommandsBuilder#setSettingsProvider`

Suppose you have a slash command `/info`:
??? note "The `/info` command"

    ```java
    public class SlashInfo extends ApplicationCommand {
        @JDASlashCommand(name = "info", subcommand = "user")
        public void userInfo(GuildSlashEvent event, @AppOption User user) {
            event.reply("User: " + user).queue();
        }
    
        @JDASlashCommand(name = "info", subcommand = "channel")
        public void channelInfo(GuildSlashEvent event, @AppOption TextChannel channel) {
            event.reply("Channel: " + channel).queue();
        }
    
        @JDASlashCommand(name = "info", subcommand = "role")
        public void roleInfo(GuildSlashEvent event, @AppOption Role role) {
            event.reply("Role: " + role).queue();
        }
    }
    ```

You then want this command to be disabled by default in every guild (so require a later manual activation by Guild moderators for example):
??? note "The `BasicSettingsProvider` class"

    ```java
    public class BasicSettingsProvider implements SettingsProvider {
        private static final Logger LOGGER = Logging.getLogger();
        private final Map<Long, List<String>> disabledCommandsMap = new HashMap<>();
        private final BContext context;
    
        public BasicSettingsProvider(BContext context) {
            this.context = context;
        }
    
        @Override
        @NotNull
        public CommandList getGuildCommands(@NotNull Guild guild) {
            return CommandList.notOf(getBlacklist(guild));
        }
    
        @NotNull
        private List<String> getBlacklist(Guild guild) {
            //Blacklist filter - the ArrayList is created only if the guild's ID was not already in the map.
            return disabledCommandsMap.computeIfAbsent(guild.getIdLong(), x -> {
                final ArrayList<String> disabledCommands = new ArrayList<>();
    
                //Let's say the info command is disabled by default
                disabledCommands.add("info");
    
                return disabledCommands;
            });
        }
    
        //This is for the part where you want to update the command list later
        // So you can use this method to "enable" an application command for a guild
        // For example in a text command
        public void addCommand(Guild guild, String commandName) {
            getBlacklist(guild).remove(commandName); //Removes the command from the blacklist
    
            //You should handle the exceptions inside the completable future, in case an error occurred
            context.scheduleApplicationCommandsUpdate(guild, false, false);
        }
    }
    ```

You can then simply set the `SettingsProvider` in `CommandsBuilder`:

??? note "How to set the settings provider"

    ```java
    var builder = CommandsBuilder.withPrefix(...)
    builder
        .setSettingsProvider(new BasicSettingsProvider(builder.getContext()))
        ...
        .build(...);
    ```