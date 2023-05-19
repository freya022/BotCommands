package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashAggregate {
    data class MyAggregate(val event: GuildSlashEvent, val string: String, val int: Int, val nestedAggregate: NestedAggregate)

    data class NestedAggregate(val event: GuildSlashEvent, val bool: Boolean)

    @CommandMarker
    fun onSlashAggregate(event: GuildSlashEvent, agg: MyAggregate) {
        event.reply_(agg.toString(), ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("aggregate", function = ::onSlashAggregate) {
            aggregate("agg", ::MyAggregate) {
                option("string")
                option("int")

                nestedAggregate("nestedAggregate", ::NestedAggregate) {
                    generatedOption("bool") { true }
                }
            }
        }
    }
}