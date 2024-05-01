# Using localization

Localization lets you translate commands & responses to the user's or the guild's language, all languages supported by Discord are supported by the framework, you can find a list of languages in JDA's [`DiscordLocale`](https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/interactions/DiscordLocale.html).

## Localization files
Localization can be stored in the JSON format and can contain your command translations, as well as command responses, or any other string.

### Where do I store them ?
Localization files must be stored in the `bc_localization` folder, in your `resources` directory, 
these files can be of any *base* name, but must end with the locale's string, and must have a `.json` extension.

Examples:

* Default localization for the `MyLocalizations` bundle: `MyLocalizations.json`
* English localization for the `MyLocalizations` bundle: `MyLocalizations_en.json`
* UK localization for the `MyLocalizations` bundle: `MyLocalizations_en_GB.json`

### What do they look like ?
The JSON file is going to be an object, with each key being the "localization key" and the value being the "localization template".

The localization key is a string where keywords are separated by a dot, the framework takes advantage of JSON as you can nest objects with their translations inside, the path to the nested translation will be your localization key, 
but they can look exactly like the keys in Java's `ResourceBundle`, where no nesting is required.

??? note "Example without nesting"

    ```json
    {
        "ban.name": "ban",
        "ban.description": "Bans an user"
    }
    ```

??? note "Example with nesting"

    ```json
    {
        "ban": {
            "name": "ban",
            "description": "Bans an user"
        }
    }
    ```

### What are localization templates ?

Localization templates are going to determine how your localized strings will include runtime values. 

The default localization templates works the same as [Java's MessageFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/MessageFormat.html), except it accepts named arguments instead of indexes.
In a nutshell, you can either have basic templates such as `This message will delete itself in {deleteTime} seconds`, or have complex templates which will accept the argument name, the format type and the format style.

??? note "Example - `/ban` success message"
    ```json title="/resources/bc_localization/MyCommandsLocalization.json"
    {
        "ban.success": "{bannedUser} was banned successfully for the reason '{reason}', and {delHours} {delHours, choice, 1#hour|2<hours} of messages were deleted"
    }
    ```

    **Don't forget** `#!java @LocalizationBundle("MyCommandsLocalization")` **to use your localization bundle** !
    
    ```java title="Ban.java"
    final String response = event.localize("ban.success",
        entry("bannedUser", targetUser.getAsMention()),
        entry("delHours", delHours), // (1)
        entry("reason", reason)
    );

    event.reply(response).queue();
    ```

    1.  The hours of messages being deleted can use different wording depending on plurality, 
        this is reflected on the localization templates with a `choice` format type, and a [choice format](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/ChoiceFormat.html)

## Localizing default messages
Default messages are messages that can be sent by the framework itself, they can come from the command listeners, components or modals for example. 

### Replacing default messages
Default messages are defined in `/resources/bc_localization/DefaultMessages.json`, this is where you can find all the localization keys used by the framework.

To override one or more default messages, create your own `DefaultMessages.json`, like you would with a normal localization file.
    
```json title="/resources/bc_localization/DefaultMessages.json"
{
    "general_error_message": "The bot has encountered an error, try again later."
}
```

### Adding translations to default messages

Default messages can also be localized, but they must keep the same base name, i.e. `DefaultMessages`.


```json title="/resources/bc_localization/DefaultMessages_fr.json"
{
    "general_error_message": "Le bot a rencontré une erreur, veuillez réessayer plus tard."
}   
```

## Localizing application commands
Only application commands supports localization, translations can include names and description of commands as well as options, and also choice names.

You will need to indicate to the framework which localization files are available, and which languages they support. This can be done with `ApplicationCommandsBuilder#addLocalizations`, such as:

``` java title="Main.java"
CommandsBuilder builder = ...;
builder.applicationCommandBuilder(applicationCommandsBuilder -> applicationCommandsBuilder
        .addLocalizations("LocalizationWikiCommands", DiscordLocale.ENGLISH_US) // (1)
);
```

1.  This enables localization from the `LocalizationWikiCommands.json` bundle, in the `en_US` language. (i.e. `LocalizationWikiCommands_en_US.json`)

    If you wish to add more localizations, add a `DiscordLocale` here, and create the corresponding files.

You can then create your commands as you would normally, no need to set up special names or anything.

Your localization keys will be the same as specified by JDA's `LocalizationFunction`, 
which means the keys are composed of the complete path, combined with the option's name and the choice's name as well, please refer to the [JDA documentation](https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/interactions/commands/localization/LocalizationFunction.html) for more details.

An example can be found [here](https://github.com/freya022/BotCommands/blob/2.X/examples/src/main/java/com/freya02/bot/wiki/localization/commands/SlashBan.java)

### Inferred command and option descriptions
Application command descriptions and option descriptions can be retrieved from the "root" localization bundles, 
i.e. localization bundles without a language specified, such as `LocalizationWikiCommands.json`

## Localizing responses

Localizing responses can be done using the framework's events, with the `localize` methods and its overloads. The method uses the best locale available, depending on the context:

* Any type of [Interaction](https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/interactions/Interaction.html): Uses the user's locale
* Other events: Uses the [Guild's locale](https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/entities/Guild.html#getLocale()) (US English by default)

Let's say someone used a ban command, but the caller cannot ban the user, due to hierarchy reasons:

```java title="SlashBan.java"
final String errorMessage = event.localize( // (1)
    "ban.caller.interact_error", 
    entry("mention", targetMember.getAsMention())  // (2)
); 
event.reply(errorMessage).queue();
```

1.  This will use the user's locale, as a slash command is an interaction.
2.  "mention" is a variable of the string template

## Example project
You can also see this very small bot using localization: [Link](https://github.com/freya022/BotCommands/tree/2.X/examples/src/main/java/com/freya02/bot/wiki/localization)