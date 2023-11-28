# About selection menus
Select menus can be of 2 types: string select menus, and, entity select menus. You can read more about them on [JDA's wiki](https://jda.wiki/using-jda/interactions/#select-menus-dropdowns).

## How can I use them ?
### Prerequisites
You will need to setup the `ComponentManager` first, in order to use these components. You can set it up [here](./The-Components-API.md)

### Creating selection menu
The `Components` class lets you create selection menus with `Components.stringSelectionMenu`, and `Components.entitySelectionMenu`.

!!! note "How to create string select menus"
    ```java
    final StringSelectMenu selectMenu = Components.stringSelectionMenu(
                ROLEPLAY_LABEL_SELECT_MENU_LISTENER, // Name of the listener, it is a constant in the class
                ThreadLocalRandom.current().nextLong() // A random number we'll pass to the listener, for demonstration
            )
            .oneUse() // (1)
            .setPlaceholder("Choose a roleplay label")
            .addOption("Option 1", "Label 1")
            .addOption("Option 2", "Label 2")
            .addOption("Option 3", "Label 3")
            .build();
    
    event.replyComponents(ActionRow.of(selectMenu)).queue();
    ```

    1.  This makes the select menu usable only once, but keep in mind that the framework won't delete it from the message.

!!! warning
    The methods from the framework (such as `oneUse` or `onTimeout`) needs to be used first. You can then use JDA's methods.

### Handling selection events
You have to make a method annotated with `#!java @JDASelectionMenuListener` 
and have their first parameter be a `StringSelectionEvent`, or a `EntitySelectionEvent`, depending on what component you are using.

Example:
```java title="SlashRoleplay.java"

private static final String ROLEPLAY_LABEL_SELECT_MENU_LISTENER = "SlashRoleplay: roleplayLabelSelectMenu"
private static final String AUTO_ROLE_SELECT_MENU_LISTENER = "SlashRoleplay: autoRoleSelectMenu"

@JDASelectionMenuListener(name = ROLEPLAY_LABEL_SELECT_MENU_LISTENER)
public void onRoleplayLabelSelected(StringSelectionEvent event, 
                                      @AppOption long randomNumber) { // Number we got back from when we created the selection menu
    event.replyFormat("My random number is %d and your labels have been set to: %s", randomNumber, event.getValues())
        .setEphemeral(true)
        .queue();
}

//If you were to create an entity select menu
@JDASelectionMenuListener(name = AUTO_ROLE_SELECT_MENU_LISTENER)
public void onAutoRoleSelected(EntitySelectionEvent event) {
    final String rolesString = event.getValues().stream()
        .map(IMentionable::getAsMention)
        .collect(Collectors.joining(", "));
    event.reply("Your roles have been set: " + rolesString).setEphemeral(true).queue();
}
```

## More examples
You can see more examples in the [examples directory](https://github.com/freya022/BotCommands/tree/2.X/examples/src/main/java/com/freya02/bot/componentsbot)