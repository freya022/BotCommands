package io.github.freya022.botcommands.test_kt.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.localize
import io.github.freya022.botcommands.api.localization.context.localizeGuild
import io.github.freya022.botcommands.api.localization.context.localizeUser
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