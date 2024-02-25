package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteManager
import io.github.freya022.botcommands.api.core.utils.awaitUnit
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.apache.commons.collections4.map.CaseInsensitiveMap
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
@Command
class SlashBasicAutocomplete : GlobalApplicationCommandProvider, AutocompleteDeclaration {
    private val fruits: Set<String> = Collections.newSetFromMap<String>(CaseInsensitiveMap()).apply {
        add("Pineapple")
        add("Apple")
        add("Pear")
    }

    suspend fun onSlashBasicAutocomplete(event: GuildSlashEvent, fruit: String) {
        if (fruit !in fruits)
            return event.reply_("Ew", ephemeral = true).awaitUnit()
        event.reply_(":yum:", ephemeral = true).awaitUnit()
    }

    fun onFruitAutocomplete(event: CommandAutoCompleteInteractionEvent, fruit: String): Collection<String> {
        if (fruit.isBlank()) return fruits
        return fruits.filter { it.startsWith(fruit, ignoreCase = true) }
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("basic_autocomplete", function = ::onSlashBasicAutocomplete) {
            option("fruit") {
                // Reference an existing autocomplete value supplier for this option
                autocompleteByFunction(::onFruitAutocomplete)
            }
        }
    }

    override fun declareAutocomplete(manager: AutocompleteManager) {
        // Register this function as an autocomplete values supplier
        // You can customize, but there's nothing to do here
        manager.autocomplete(::onFruitAutocomplete)
    }
}