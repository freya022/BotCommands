# Updating existing commands on the fly

Let's say a Guild moderator decides to enable the /info command in his Guild, you would need to take your `BasicSettingsProvider` (from `BContext#getSettingsProvider`), cast it to your class and then use a method to remove the command from the Guild's blacklist, then finally call `BContext#scheduleApplicationCommandsUpdate` to update the commands and the slash commands local cache

!!! note "How to enable back the command and update the slash commands list - Using a regular command"

    ```java
    @Category("Moderation")
    @UserPermissions(Permission.MANAGE_ROLES)
    public class EnableInfoCommand extends TextCommand {
        @JDATextCommand(
                name = "enableinfocommand",
                description = "Enables the /info command"
        )
        public void execute(CommandEvent event) {
            if (event.getMember().canInteract(event.getGuild().getSelfMember())) {
                final BasicSettingsProvider settingsProvider = (BasicSettingsProvider) event.getContext().getSettingsProvider();
    
                if (settingsProvider == null) {
                    event.indicateError("No settings provider has been set").queue();
    
                    return;
                }
    
                settingsProvider.addCommand(event.getGuild(), "info");
    
                event.reactSuccess().queue();
            } else {
                event.indicateError("You cannot do this").queue();
            }
        }
    }
    ```