package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji

@Command
class TextHelpTesting : TextCommand() {
    @JDATextCommand(name = "help_testing")
    fun onTextHelpTesting(event: BaseCommandEvent,
                          @TextOption boolean: Boolean,
                          @TextOption double: Double,
                          @TextOption emoji: Emoji,
                          @TextOption guild: Guild,
                          @TextOption int: Int,
                          @TextOption long: Long,
                          @TextOption member: Member,
                          @TextOption user: User,
                          @TextOption role: Role,
                          @TextOption string: String,
    ) {
        event.respond("testing").queue()
    }
}