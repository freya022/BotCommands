package io.github.freya022.botcommands.internal.localization.interaction.autoconfigure

import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction

internal object DefaultGuildLocaleProvider : GuildLocaleProvider {

    override fun getDiscordLocale(interaction: Interaction): DiscordLocale = interaction.guildLocale
}
