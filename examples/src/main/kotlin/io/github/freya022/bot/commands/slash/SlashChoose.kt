package io.github.freya022.bot.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.bot.switches.FrontendChooser
import io.github.freya022.bot.switches.SimpleFrontend
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService

@BService
class SlashChoose {
    fun onSlashChoose(event: GuildSlashEvent, choices: List<String>) {
        event.reply_(choices.random(), ephemeral = true).queue()
    }
}

@Command
@ConditionalService(FrontendChooser::class)
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
@SimpleFrontend
@ConditionalService(FrontendChooser::class)
class SlashChooseSimplifiedFront(private val slashChoose: SlashChoose) : ApplicationCommand() {
    @JDASlashCommand(name = "choose", description = "Randomly choose a value")
    fun onSlashBan(
        event: GuildSlashEvent,
        // Notice here how you are limited to 1 description for all your options
        @SlashOption(name = "choice", description = "A choice") @VarArgs(10, numRequired = 2) choices: List<String>
    ) = slashChoose.onSlashChoose(event, choices)
}