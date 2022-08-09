package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.annotations.api.application.annotations.Test
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import com.freya02.botcommands.core.api.annotations.LateService
import com.freya02.botcommands.internal.lockableNotNull
import net.dv8tion.jda.api.entities.Guild
import java.util.*
import kotlin.properties.Delegates

@LateService
class BApplicationConfig internal constructor(config: BConfig) {
    val slashGuildIds: MutableList<Long> = mutableListOf()
    val testGuildIds: MutableList<Long> = mutableListOf()
    var onlineAppCommandCheckEnabled: Boolean by Delegates.lockableNotNull(config, defaultVal = false)
    var forceGuildCommands: Boolean by Delegates.lockableNotNull(config, defaultVal = false)

    private val baseNameToLocalesMap: MutableMap<String, MutableList<Locale>> = hashMapOf()

    /**
     * Debug feature - Makes it so application commands are only updated on these guilds
     *
     * @param slashGuildIds IDs of the guilds
     */
    fun updateCommandsOnGuildIds(slashGuildIds: List<Long>) {
        this.slashGuildIds.clear()
        this.slashGuildIds.addAll(slashGuildIds)
    }

    /**
     * Adds test guilds IDs for all commands annotated with [Test]
     *
     * @param guildIds The test [Guild] IDs
     *
     * @see BContext.getTestGuildIds
     * @see Test
     */
    fun addTestGuilds(guildIds: List<Long>) {
        this.testGuildIds += guildIds
    }

    /**
     * Enables the library to do network calls to Discord in order to check if application commands need to be updated
     *
     * It's better to leave it disk-based, it is faster and doesn't require any request to Discord
     *
     * **Online checks are to be avoided on production environments**, I strongly recommend you have a separate bot for tests purpose
     *
     *
     * This option only makes sense if you work on your "development" bot is on multiple computers,
     * as the files required for caching the already-pushed-commands are stored in your temporary files folder,
     * another computer is not aware of it and might take *its own* files as being up-to-date, even if the commands on Discord are not.
     *
     * This issue is fixed by using online checks
     */
    fun enableOnlineAppCommandCheck() {
        this.onlineAppCommandCheckEnabled = true
    }

    /**
     * Sets whether all application commands should be guild-only, regardless of the command scope on the annotation
     *
     * @param force `true` to make all application commands as guild-only
     */
    fun forceCommandsAsGuildOnly(force: Boolean) {
        this.forceGuildCommands = force
    }

    /**
     * Adds the specified bundle names with its locales, those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle, logging a warning if it can't be found
     *
     * See [DefaultLocalizationMapProvider] for default implementation details
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     */
    fun addLocalizations(bundleName: String, vararg locales: Locale) {
        addLocalizations(bundleName, locales.asList())
    }

    /**
     * Adds the specified bundle names with its locales, those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle, logging a warning if it can't be found
     *
     * See [DefaultLocalizationMapProvider] for default implementation details
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     */
    fun addLocalizations(bundleName: String, locales: List<Locale>) {
        baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() }.addAll(locales)
    }
}