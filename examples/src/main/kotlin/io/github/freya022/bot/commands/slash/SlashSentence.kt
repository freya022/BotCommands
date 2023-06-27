package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.core.annotations.Handler
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

private const val sentencePartAutocompleteName = "SlashSentence: sentencePart"

@Handler // Because we have an autocomplete handler
class SlashSentence {
    @JvmInline
    value class SentenceParts(private val parts: List<String>) {
        fun assemble() = parts.joinToString(separator = " ", postfix = ".")
        fun toChoiceContinuation(query: String) = Choice(parts.joinToString(separator = " ", postfix = "."), query)
    }

    // Demo about autocomplete and inline classes w/ varargs
    // Autocomplete will show the current state of the sentence + the input you're trying to add
    fun onSlashSentence(event: GuildSlashEvent, parts: SentenceParts) {
        event.reply_(parts.assemble(), ephemeral = true).queue()
    }

    // The autocomplete will only work once the user inputs at least 2 strings,
    // since this is the amount we required on the vararg
    @CacheAutocomplete(
        // Let's just say we cache results based on the two first parts,
        // caching here is absolutely pointless, this is just for the example.
        // Notice how we can use an option which isn't used by the autocomplete function
        compositeKeys = ["part_0", "part_1"],
        // Force the autocomplete cache for the sake of demonstration,
        // as it is disabled on dev builds, as defined in Main
        forceCache = true
    )
    @AutocompleteHandler(name = sentencePartAutocompleteName, showUserInput = false)
    fun onSentencePartAutocomplete(event: CommandAutoCompleteInteractionEvent, parts: SentenceParts) = when {
        event.focusedOption.value.isEmpty() -> listOf() //Discord isn't going to like empty choices
        else -> listOf(parts.toChoiceContinuation(event.focusedOption.value))
    }
}

@Command
// Comment this and uncomment the condition for SlashSentenceSimplifiedFront if you want to switch front,
// even though they produce the same command, minus the aggregated object
@ConditionalService(DisableFrontend::class)
class SlashSentenceDetailedFront {
    @AppDeclaration
    fun onDeclare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("sentence", function = SlashSentence::onSlashSentence) {
            description = "Make a sentence"

            inlineClassOptionVararg<SlashSentence.SentenceParts>(
                declaredName = "parts",
                amount = 10,
                requiredAmount = 2,
                optionNameSupplier = { count -> "part_$count" }
            ) { count ->
                description = "Sentence part N°$count"

                autocompleteReference(sentencePartAutocompleteName)
            }
        }
    }
}

@Command
//@ConditionalService(DisableFrontend::class)
class SlashSentenceSimplifiedFront(private val slashSentence: SlashSentence) : ApplicationCommand() {
    @JDASlashCommand(name = "sentence", description = "Make a sentence")
    fun onSlashSentence(
        event: GuildSlashEvent,
        // Notice here how you are limited to 1 description for all your options
        @AppOption(name = "part", description = "A sentence part", autocomplete = sentencePartAutocompleteName) @VarArgs(10, numRequired = 2) parts: SlashSentence.SentenceParts,
    ) = slashSentence.onSlashSentence(event, parts)
}