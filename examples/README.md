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