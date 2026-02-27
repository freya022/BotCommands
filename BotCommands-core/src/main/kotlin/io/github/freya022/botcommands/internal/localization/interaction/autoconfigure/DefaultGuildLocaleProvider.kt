package io.github.freya022.botcommands.internal.localization.interaction.autoconfigure

import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

internal object DefaultGuildLocaleProvider : GuildLocaleProvider {

    @Deprecated("Use 'getLocale' instead, use 'DiscordLocale.from' / 'DiscordLocale#toLocale' if necessary")
    override fun getDiscordLocale(interaction: Interaction): DiscordLocale = interaction.guildLocale

    override fun getLocale(interaction: Interaction): Locale = interaction.guildLocale.toLocale()
}
