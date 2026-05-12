package io.github.freya022.botcommands.internal.localization.interaction.autoconfigure

import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

internal object DefaultGuildLocaleProvider : GuildLocaleProvider {

    override fun getLocale(interaction: Interaction): Locale = interaction.guildLocale.toLocale()
}
