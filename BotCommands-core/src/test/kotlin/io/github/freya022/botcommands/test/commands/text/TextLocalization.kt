package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.localization.text.replyGuild
import io.github.freya022.botcommands.api.localization.text.respondLocalized
import net.dv8tion.jda.api.interactions.DiscordLocale

@Command
class TextLocalization : TextCommand() {
    @JDATextCommandVariation(path = ["localization"])
    fun onTextLocalization(event: BaseCommandEvent) {
        event.localizationPrefix = "commands.localization"

        event.replyGuild("response", "guild_users" to event.guild.memberCount, "uptime" to 3.141519).queue()
        event.respondLocalized(DiscordLocale.GERMAN, "response", "guild_users" to event.guild.memberCount, "uptime" to 3.141519).queue()
    }
}