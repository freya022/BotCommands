package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*

@InjectedService
interface BApplicationConfig {
    /**
     * If not empty, only these guilds will have their application commands updated.
     *
     * Existing commands won't be removed in other guilds, global commands will still be updated.
     *
     * Spring property: `botcommands.application.slashGuildIds`
     */
    val slashGuildIds: List<Long>

    /**
     * Test guilds IDs for all commands annotated with [Test]
     *
     * Spring property: `botcommands.application.testGuildIds`
     *
     * @see Test @Test
     */
    val testGuildIds: List<Long>

    /**
     * Enables the library to compare local commands against Discord's command,
     * to check if application commands need to be updated.
     *
     * The default behavior is to compare the command data to what has been locally saved,
     * as it does not require any request, and is therefore way faster.
     *
     * The issue with local checks is that you could update commands on another machine,
     * while the other machine is not aware of it.
     *
     * Which is why you should use online checks during development,
     * but local checks in production, as they avoid requests and aren't run on multiple machines.
     *
     * **Note**: This does not enable you to run two instances simultaneously.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.onlineAppCommandCheckEnabled`
     */
    val onlineAppCommandCheckEnabled: Boolean

    /**
     * Sets whether all application commands should be guild-only, regardless of the command scope on the annotation.
     *
     * **Beware**: This also means that your global application commands will **not** be registered.
     *
     * **Note:** This only works on **annotated** commands,
     * as you can return when manually declaring with the DSL
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.forceGuildCommands`
     */
    val forceGuildCommands: Boolean

    /**
     * Mappings between the base bundle name and the locales it supports.
     *
     * For example: `MyCommands` -> `[Locale.GERMAN, Locale.FRENCH]`
     * will, by default, find bundles `MyCommands_de_DE.json` and `MyCommands_fr_FR.json`.
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     *
     * Spring property: `botcommands.application.baseNameToLocalesMap`
     */
    val baseNameToLocalesMap: Map<String, List<Locale>>
}

@ConfigDSL
class BApplicationConfigBuilder internal constructor() : BApplicationConfig {
    override val slashGuildIds: MutableList<Long> = mutableListOf()
    override val testGuildIds: MutableList<Long> = mutableListOf()
    @set:DevConfig
    @set:JvmName("enableOnlineAppCommandChecks")
    override var onlineAppCommandCheckEnabled: Boolean = false
    @set:DevConfig
    @set:JvmName("forceGuildCommands")
    override var forceGuildCommands: Boolean = false

    private val _baseNameToLocalesMap: MutableMap<String, MutableList<Locale>> = hashMapOf()
    override val baseNameToLocalesMap: Map<String, List<Locale>>
        get() = _baseNameToLocalesMap.toImmutableMap()

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found
     *
     * For example: `MyCommands` -> `[Locale.GERMAN, Locale.FRENCH]`
     * will, by default, find bundles `MyCommands_de_DE.json` and `MyCommands_fr_FR.json`.
     *
     * See [LocalizationFunction] on how your command localization keys need to be constructed
     *
     * See [DefaultLocalizationMapProvider] and [DefaultJsonLocalizationMapReader] for default implementation details
     *
     * Spring property: `botcommands.application.baseNameToLocalesMap`
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     * @see LocalizationFunction
     */
    fun addLocalizations(bundleName: String, locales: List<DiscordLocale>) {
        _baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() } += locales.map(DiscordLocale::toLocale)
    }

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found
     *
     * For example: `MyCommands` -> `[Locale.GERMAN, Locale.FRENCH]`
     * will, by default, find bundles `MyCommands_de_DE.json` and `MyCommands_fr_FR.json`.
     *
     * See [LocalizationFunction] on how your command localization keys need to be constructed
     *
     * See [DefaultLocalizationMapProvider] and [DefaultJsonLocalizationMapReader] for default implementation details
     *
     * Spring property: `botcommands.application.baseNameToLocalesMap`
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     * @see LocalizationFunction
     */
    fun addLocalizations(bundleName: String, vararg locales: DiscordLocale) {
        addLocalizations(bundleName, locales.asList())
    }

    @JvmSynthetic
    internal fun build() = object : BApplicationConfig {
        override val slashGuildIds = this@BApplicationConfigBuilder.slashGuildIds.toImmutableList()
        override val testGuildIds = this@BApplicationConfigBuilder.testGuildIds.toImmutableList()
        override val onlineAppCommandCheckEnabled = this@BApplicationConfigBuilder.onlineAppCommandCheckEnabled
        override val forceGuildCommands = this@BApplicationConfigBuilder.forceGuildCommands
        override val baseNameToLocalesMap =
            this@BApplicationConfigBuilder.baseNameToLocalesMap.mapValues { (_, v) -> v.toImmutableList() }
                .toImmutableMap()
    }
}