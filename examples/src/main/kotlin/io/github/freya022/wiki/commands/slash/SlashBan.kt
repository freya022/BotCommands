package io.github.freya022.wiki.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger { }

// --8<-- [start:aggregated_object-kotlin]
// This data class is practically pointless;
// this is just to demonstrate how you can group parameters together,
// so you can benefit from functions/backed properties limited to your parameters,
// without polluting classes with extensions
data class DeleteTimeframe(val time: Long, val unit: TimeUnit) {
    override fun toString(): String = "$time ${unit.name.lowercase()}"
}
// --8<-- [end:aggregated_object-kotlin]

@BService
class SlashBan {
    fun onSlashBan(
        event: GuildSlashEvent,
        timeframe: DeleteTimeframe
    ) {
        throw UnsupportedOperationException()
    }
}

@Command
class SlashBanDetailedFront {
    @AppDeclaration
    fun onDeclare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("ban", function = SlashBan::onSlashBan) {
            // ...

            // --8<-- [start:declare_aggregate-kotlin_dsl]
            aggregate(declaredName = "timeframe", aggregator = ::DeleteTimeframe) {
                option(declaredName = "time") {
                    description = "The timeframe of messages to delete with the specified unit"
                }

                option(declaredName = "unit") {
                    description = "The unit of the delete timeframe"

                    usePredefinedChoices = true
                }
            }
            // --8<-- [end:declare_aggregate-kotlin_dsl]

            // ...
        }
    }
}