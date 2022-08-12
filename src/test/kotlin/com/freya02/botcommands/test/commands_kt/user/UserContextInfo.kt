package com.freya02.botcommands.test.commands_kt.user

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@CommandMarker
class UserContextInfo : ApplicationCommand() {
    @CommandMarker
    fun onUserContextInfo(event: GlobalUserEvent, user: User, userTag: String) {
        event.reply_("Tag of user ID ${user.id}: $userTag", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.userCommand("User info") {
            option("user")

            generatedOption("userTag") {
                it as UserContextInteractionEvent

                it.target.asTag
            }

            function = ::onUserContextInfo
        }
    }
}