package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.localization.Localization.Entry.entry
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.interactions.DiscordLocale

@CommandMarker
class SlashLocalization : ApplicationCommand() {
    @JDASlashCommand(name = "localization")
    fun onSlashLocalization(event: GuildSlashEvent,
                            @LocalizationBundle("Test", prefix = "commands.localization") ctx: AppLocalizationContext,
                            @AppOption localizationOpt: String?) {
        val content = """
            User localized (${ctx.userLocale}): %s
            Guild localized (${ctx.guildLocale}): %s
            German localized: %s
        """.trimIndent().format(
            ctx.localizeUser("response",
                entry("guild_users", event.guild.memberCount),
                entry("uptime", 3.141519)),
            ctx.localizeGuild("response",
                entry("guild_users", event.guild.memberCount),
                entry("uptime", 3.141519)),
            ctx.localize(DiscordLocale.GERMAN, "response",
                entry("guild_users", event.guild.memberCount),
                entry("uptime", 3.141519))
        )

        event.reply_(content, ephemeral = true).queue()
    }
}