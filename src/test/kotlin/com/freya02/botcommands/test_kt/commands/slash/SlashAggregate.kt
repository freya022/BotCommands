package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@CommandMarker
class SlashAggregate {
    @JvmInline
    value class MyInlineString(val yes: String)

    //Upcast is required as these can be constructed with either a slash command, or autocomplete
    data class MyAggregate(val string: String, val int: Int, val ints: List<Int>, val nestedAggregate: NestedAggregate)

    data class NestedAggregate(val bool: Boolean, val nestedDouble: Double)

    @CommandMarker
    fun onSlashAggregate(event: GuildSlashEvent, agg: MyAggregate, inlineAutoStr: MyInlineString) {
        event.reply_("$agg + $inlineAutoStr", ephemeral = true).queue()
    }

    @CommandMarker
    fun onInlineAutoStrAutocomplete(event: CommandAutoCompleteInteractionEvent, agg: MyAggregate) =
        listOf(agg.toString().substring(0..<100))

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
                autocomplete("SlashAggregate: inlineAutoStr", ::onInlineAutoStrAutocomplete) {
                    this.showUserInput = false
                }
            }
        }
    }
}