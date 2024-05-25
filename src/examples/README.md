## Running the examples

> [!NOTE]
> This bot is for documentation purposes

### Additional requirements

* A PostgreSQL database
* Your bot token

### Configuration
Go to `/test-files/examples` from the project root, duplicate the `config-template` folder as `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

You can then run the `Main` class.

## Examples

### Commands
* [A ban command which can be declared using annotations or manually](kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt), see `SlashBanDetailedFront#onDeclare`
* [A ban command with full localization support](kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt)
* [A ban command with an aggregate parameter](kotlin/io/github/freya022/bot/commands/slash/SlashBan.kt), see `aggregate` in `SlashBanDetailedFront#onDeclare`
* [A choose command using varargs](kotlin/io/github/freya022/bot/commands/slash/SlashChoose.kt)
* [An alternative handler when a text command is used incorrectly (help command replacement)](kotlin/io/github/freya022/bot/commands/text/HelpCommand.kt)
* [A choose command which autocompletes your sentence with previous words](kotlin/io/github/freya022/bot/commands/slash/SlashSentence.kt), on `SlashSentence#onSentencePartAutocomplete`
* [A choose command using inline classes](kotlin/io/github/freya022/bot/commands/slash/SlashSentence.kt), with `SlashSentence.SentenceParts`
 
### Services
* [How to automatically load JDA with a service](kotlin/io/github/freya022/bot/Bot.kt)
* [How to make a strategy-based ban service, using dynamic service suppliers](kotlin/io/github/freya022/bot/commands/ban/BanService.kt)

### Components
* [A self-destructing button](kotlin/io/github/freya022/bot/commands/slash/SlashButton.kt)

### Modals
* [A modal to format your code](kotlin/io/github/freya022/bot/commands/slash/SlashModal.kt)

### Event listeners
* [A listener which prints stats about the bot, when JDA is ready](kotlin/io/github/freya022/bot/ReadyListener.kt)