## Running the examples

### Additional requirements

* A PostgreSQL database
* Your bot token

### Configuration
You need to copy the `config-template` folder's content in `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

No need to keep the `logback.xml`, only `logback-test.xml` is necessary.

[//]: # (https://tree.nathanfriend.io/?s=%28%27options%21%28%27fancy%21true%7EfullPath%21false%7EtrailingSlash%21true%7ErootDot%21false%29%7E*%28%27*%27%27%29%7Eversion%21%271%27%29*source%21%01*)

Your file tree should look like this:
```
./
└── examples/
    ├── dev-config/
    │   ├── config.json
    │   └── logback-test.xml
    ├── src/
    │   └── ..
    └── pom.xml
```

You can then just run the `Main` class.

### Starting your own bot
If you are seeking for a bot you can set up yourself, you can look at [the bot template](../BotTemplate).

## Examples

### Commands
* [A ban command which can be declared using annotations or manually](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt), see `SlashBanDetailedFront#onDeclare`
* [A ban command with full localization support](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt)
* [A ban command with an aggregate parameter](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt), see `aggregate` in `SlashBanDetailedFront#onDeclare`
* [A choose command using varargs](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashChoose.kt)
* [An alternative handler when a text command is used incorrectly (help command replacement)](src/main/kotlin/io/github/freya022/bot/commands/text/HelpCommand.kt)
* [A choose command which autocompletes your sentence with previous words](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashSentence.kt), on `SlashSentence#onSentencePartAutocomplete`
* [A choose command using inline classes](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashSentence.kt), with `SlashSentence.SentenceParts`
 
### Services
* [How to automatically load with a service](src/main/kotlin/io/github/freya022/bot/JDAService.kt)
* [How to make a strategy-based ban service, using dynamic service suppliers](src/main/kotlin/io/github/freya022/bot/commands/ban/BanService.kt)

### Components
* [A self-destructing button](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashButton.kt)

### Modals
* [A modal to format your code](src/main/kotlin/io/github/freya022/bot/commands/slash/SlashModal.kt)

### Event listeners
* [A listener which prints stats about the bot, when JDA is ready](src/main/kotlin/io/github/freya022/bot/ReadyListener.kt)