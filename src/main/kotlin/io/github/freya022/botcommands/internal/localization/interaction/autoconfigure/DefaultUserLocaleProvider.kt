package io.github.freya022.botcommands.internal.localization.interaction.autoconfigure

import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction

internal object DefaultUserLocaleProvider : UserLocaleProvider {

    override fun getDiscordLocale(interaction: Interaction): DiscordLocale = interaction.userLocale
}
