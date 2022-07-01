package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.annotations.Name
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.ValueRange.Companion.range
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.Command

class MyCommand : ApplicationCommand() {
    @CommandMarker
    fun executeCommand(
        event: GuildSlashEvent,
        stringOption: String,
        @Name("int", "notIntOption") intOption: Int,
        @Name(name = "user") userOption: User,
        @ChannelTypes(ChannelType.CATEGORY) channelOptionAnnot: Category,
        channelOption: TextChannel,
        @Name(declaredName = "notDoubleOption") doubleOption: Double?,
        custom: BContext
    ) {
        event.reply(stringOption + intOption + doubleOption + userOption + custom + channelOptionAnnot + channelOption).queue()
    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        for ((subname, localFunction) in mapOf("kt" to ::executeCommand, "java" to MyJavaCommand::cmd)) {
            manager.slashCommand(CommandPath.of("my_command", subname)) {
                scope = CommandScope.GUILD
                description = "mah desc"

                option("stringOption") {
                    description = "Option description"

                    choices = listOf(Command.Choice("a", "a"), Command.Choice("b", "b"), Command.Choice("c", "c"))
                }

                option("notIntOption") {
                    description = "An integer"

                    valueRange = 1 range 2
                }

                option("notDoubleOption") {
                    description = "A double"
                }

                option("userOption") {
                    description = "An user"
                }

                option("channelOptionAnnot")

                option("channelOption") {
                    channelTypes = enumSetOf(ChannelType.TEXT)
                }

                customOption("custom")

                function = localFunction
            }
        }
    }
}