package io.github.freya022.botcommands.test_kt

import io.github.freya022.botcommands.api.core.SettingsProvider
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.DiscordLocale

@BService
class BasicSettingsProvider : SettingsProvider {
    override fun getLocale(guild: Guild?): DiscordLocale {
        if (guild?.idLong == 722891685755093072L) {
            return DiscordLocale.ENGLISH_UK //not default on my system
        }

        return super.getLocale(guild)
    }

    override fun doesUserConsentNSFW(user: User): Boolean {
        return true
    }
}