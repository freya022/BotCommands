package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:word_command-kotlin]
@Command
class SlashWord : ApplicationCommand() {
    @JDASlashCommand(name = "word", description = "Autocompletes a word")
    suspend fun onSlashWord(
        event: GuildSlashEvent,
        @SlashOption(description = "The word", autocomplete = SlashWordAutocomplete.WORD_AUTOCOMPLETE_NAME) word: String,
    ) {
        event.reply_("Your word was $word", ephemeral = true).await()
    }
}
// --8<-- [end:word_command-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:word_autocomplete-kotlin]
@Handler // Required by the AutocompleteHandler annotation, can be replaced with @Command
class SlashWordAutocomplete {
    // https://en.wikipedia.org/wiki/Dolch_word_list#Dolch_list:_Nouns
    // but 30 words
    private val words = listOf(
        "apple", "baby", "back", "ball", "bear", "bed", "bell", "bird", "birthday", "boat",
        "box", "boy", "bread", "brother", "cake", "car", "cat", "chair", "chicken", "children",
        "Christmas", "coat", "corn", "cow", "day", "dog", "doll", "door", "duck", "egg"
    )

    // You can also make this return a collection of Choice, see the annotation docs
    @AutocompleteHandler(WORD_AUTOCOMPLETE_NAME)
    fun onWordAutocomplete(event: CommandAutoCompleteInteractionEvent): Collection<String> {
        // Here you would typically filter the words based on what the user inputs,
        // but it is already done when you return a Collection<String>
        return words
    }

    companion object {
        const val WORD_AUTOCOMPLETE_NAME = "SlashWord: word"
    }
}
// --8<-- [end:word_autocomplete-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:word_command-kotlin_dsl]
@Command
class SlashWordDsl : GlobalApplicationCommandProvider {
    suspend fun onSlashWord(event: GuildSlashEvent, word: String) {
        event.reply_("Your word was $word", ephemeral = true).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("word", function = ::onSlashWord) {
            description = "Autocompletes a word"

            option("word") {
                description = "The word"

                // Use an existing autocomplete declaration
                autocompleteByFunction(SlashWordAutocompleteDsl::onWordAutocomplete)
            }
        }
    }
}
// --8<-- [end:word_command-kotlin_dsl]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:word_autocomplete-kotlin_dsl]
@Handler // As we only declare autocomplete handlers
class SlashWordAutocompleteDsl : AutocompleteHandlerProvider {
    // https://en.wikipedia.org/wiki/Dolch_word_list#Dolch_list:_Nouns
    // but 30 words
    private val words = listOf(
        "apple", "baby", "back", "ball", "bear", "bed", "bell", "bird", "birthday", "boat",
        "box", "boy", "bread", "brother", "cake", "car", "cat", "chair", "chicken", "children",
        "Christmas", "coat", "corn", "cow", "day", "dog", "doll", "door", "duck", "egg"
    )

    // You can also make this return a collection of Choice, see the AutocompleteManager#autocomplete docs
    fun onWordAutocomplete(event: CommandAutoCompleteInteractionEvent): Collection<String> {
        // Here you would typically filter the words based on what the user inputs,
        // but it is already done when you return a Collection<String>
        return words
    }

    // All autocomplete declarations run before any command is registered,
    // so you can, in theory, add autocomplete handlers anywhere,
    // and use them in any command.
    override fun declareAutocomplete(manager: AutocompleteManager) {
        manager.autocomplete(::onWordAutocomplete)
    }
}
// --8<-- [end:word_autocomplete-kotlin_dsl]