package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.localize
import com.freya02.botcommands.api.localization.context.localizeGuild
import com.freya02.botcommands.api.localization.context.localizeUser
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.interactions.DiscordLocale

@Command
class SlashLocalization : ApplicationCommand() {
    @JDASlashCommand(name = "localization")
    fun onSlashLocalization(event: GuildSlashEvent,
                            @LocalizationBundle("Test", prefix = "commands.localization") ctx: AppLocalizationContext,
                            @SlashOption localizationOpt: String?) {
        val content = """
            User localized (${ctx.userLocale}): %s
            Guild localized (${ctx.guildLocale}): %s
            German localized: %s
        """.trimIndent().format(
            ctx.localizeUser("response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519),
            ctx.localizeGuild("response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519),
            ctx.localize(DiscordLocale.GERMAN, "response",
                "guild_users" to event.guild.memberCount,
                "uptime" to 3.141519)
        )

        event.reply_(content, ephemeral = true).queue()
    }
}