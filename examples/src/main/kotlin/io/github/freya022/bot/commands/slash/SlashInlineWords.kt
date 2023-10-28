package io.github.freya022.bot.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

// --8<-- [start:inline_sentence-kotlin]
private val spaceDelimiter = Regex("""\s+""")

@JvmInline
value class Sentence(val value: String) {
    val words: List<String> get() = spaceDelimiter.split(value)
}

@Command
class SlashInlineWords : ApplicationCommand() {
    @JDASlashCommand(name = "words", description = "Extracts the words of a sentence")
    suspend fun onSlashWords(event: GuildSlashEvent, @SlashOption(description = "Input sentence") sentence: Sentence) {
        event.reply_("The words are: ${sentence.words}", ephemeral = true).await()
    }
}
// --8<-- [end:inline_sentence-kotlin]