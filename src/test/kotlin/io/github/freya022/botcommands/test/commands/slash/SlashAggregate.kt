package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.annotations.CommandMarker
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteManager
import io.github.freya022.botcommands.test.CustomObject
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@Command
class SlashAggregate : AutocompleteDeclaration {
    @JvmInline
    value class MyInlineString(val yes: String)

    //Upcast is required as these can be constructed with either a slash command, or autocomplete
    data class MyAggregate(val string: String, val int: Int, val ints: List<Int>, val nestedAggregate: NestedAggregate)

    data class NestedAggregate(val bool: Boolean, val nestedDouble: Double)

    @CommandMarker
    fun onSlashAggregate(event: GuildSlashEvent, agg: MyAggregate, inlineAutoStr: MyInlineString, customObject: CustomObject) {
        event.reply_("$agg + $inlineAutoStr", ephemeral = true).queue()
    }

    @CommandMarker
    fun onInlineAutoStrAutocomplete(
        event: CommandAutoCompleteInteractionEvent,
        agg: MyAggregate,
        customObject: CustomObject
    ) = listOf(customObject) //return custom object, to test autocomplete transformers

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("aggregate", function = ::onSlashAggregate) {
            aggregate("agg", ::MyAggregate) {
                option("string")
                option("int")

                nestedOptionVararg("ints", 2, 1, { "int_$it" })

                nestedAggregate("nestedAggregate", ::NestedAggregate) {
                    option("nestedDouble")
                    generatedOption("bool") { true }
                }
            }

            inlineClassOption<MyInlineString>("inlineAutoStr") {
                autocompleteByFunction(::onInlineAutoStrAutocomplete)
            }

            customOption("customObject")
        }
    }

    override fun declare(manager: AutocompleteManager) {
        manager.autocomplete(::onInlineAutoStrAutocomplete) {
            this.showUserInput = false

            cache(AutocompleteCacheMode.CONSTANT_BY_KEY) {
                compositeKeys = listOf("string", "nestedDouble")
            }
        }
    }
}