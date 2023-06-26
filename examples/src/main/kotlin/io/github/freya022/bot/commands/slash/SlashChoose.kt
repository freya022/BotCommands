package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import dev.minn.jda.ktx.messages.reply_

@BService
class SlashChoose {
    fun onSlashChoose(event: GuildSlashEvent, choices: List<String>) {
        event.reply_(choices.random(), ephemeral = true).queue()
    }
}

@Command
// Comment this and uncomment the condition for SlashChooseSimplifiedFront if you want to switch front,
// even though they produce the same command, minus the aggregated object
//@ConditionalService(DisableFrontend::class)
//TODO keep those annotations enabled and make a switch in the checker once we are able to access the instantiated class
// as to be able to switch between simplified and detailed versions across the whole bot
class SlashChooseDetailedFront {
    @AppDeclaration
    fun onDeclare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("choose", function = SlashChoose::onSlashChoose) {
            description = "Randomly choose a value"

            optionVararg(
                declaredName = "choices",
                amount = 10,
                requiredAmount = 2,
                optionNameSupplier = { count -> "choice_$count" }
            ) { count ->
                description = "Choice N°$count"
            }
        }
    }
}

@Command
@ConditionalService(DisableFrontend::class)
class SlashChooseSimplifiedFront(private val slashChoose: SlashChoose) : ApplicationCommand() {
    @JDASlashCommand(name = "choose", description = "Randomly choose a value")
    fun onSlashBan(
        event: GuildSlashEvent,
        // Notice here how you are limited to 1 description for all your options
        @AppOption(name = "choice", description = "A choice") @VarArgs(10, numRequired = 2) choices: List<String>
    ) = slashChoose.onSlashChoose(event, choices)
}