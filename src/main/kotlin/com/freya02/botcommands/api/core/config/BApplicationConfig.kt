package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.commands.application.ApplicationCommandFilter
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.utils.toImmutableList
import com.freya02.botcommands.api.core.utils.toImmutableMap
import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import com.freya02.botcommands.internal.core.config.ConfigDSL
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*
import kotlin.reflect.KClass

@InjectedService
interface BApplicationConfig {
    /**
     * Enables you to push application commands are only updated on these guilds
     */
    val slashGuildIds: List<Long>

    /**
     * Test guilds IDs for all commands annotated with [Test]
     *
     * @see Test
     */
    val testGuildIds: List<Long>

    /**
     * Enables the library to compare local commands against Discord's command,
     * in order to check if application commands need to be updated.
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
     */
    val onlineAppCommandCheckEnabled: Boolean

    /**
     * Sets whether all application commands should be guild-only, regardless of the command scope on the annotation.
     *
     * **Note:** This only works on **annotated** commands,
     * as you can simply return when manually declaring with the DSL
     *
     * Default: `false`
     */
    val forceGuildCommands: Boolean

    /**
     * A list of application command filters, if one of them returns false, the command is not executed.
     *
     * **You still have to acknowledge to the interaction !**
     */
    val applicationFilters: List<ApplicationCommandFilter>

    val baseNameToLocalesMap: Map<String, List<Locale>>
}

@ConfigDSL
class BApplicationConfigBuilder internal constructor(private val serviceConfig: BServiceConfigBuilder) : BApplicationConfig {
    override val slashGuildIds: MutableList<Long> = mutableListOf()
    override val testGuildIds: MutableList<Long> = mutableListOf()
    override var onlineAppCommandCheckEnabled: Boolean = false
    override var forceGuildCommands: Boolean = false
    override val applicationFilters: MutableList<ApplicationCommandFilter> = arrayListOf()

    private val _baseNameToLocalesMap: MutableMap<String, MutableList<Locale>> = hashMapOf()
    override val baseNameToLocalesMap: Map<String, List<Locale>>
        get() = _baseNameToLocalesMap.toImmutableMap()

    /**
     * Registers an autocomplete transformer.
     *
     * If your autocomplete handler return a `List<YourObject>`,
     * you will have to register an `AutocompleteTransformer<YourObject>`
     *
     * @param transformerType The type of the transformer service, which will transform an element of a [List], into a [Choice].
     *
     * @return This builder for chaining convenience
     */
    fun registerAutocompleteTransformer(transformerType: Class<AutocompleteTransformer<*>>) =
        registerAutocompleteTransformer(transformerType.kotlin)

    /**
     * Registers an autocomplete transformer.
     *
     * If your autocomplete handler return a `List<YourObject>`,
     * you will have to register an `AutocompleteTransformer<YourObject>`
     *
     * @param transformerType The type of the transformer service, which will transform an element of a [List], into a [Choice].
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    fun registerAutocompleteTransformer(transformerType: KClass<AutocompleteTransformer<*>>) {
        serviceConfig.registerService(transformerType)
    }

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found
     *
     * See [LocalizationFunction] on how your command localization keys need to be constructed
     *
     * See [DefaultLocalizationMapProvider] for default implementation details
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     * @see LocalizationFunction
     */
    fun addLocalizations(bundleName: String, locales: List<DiscordLocale>) {
        _baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() } += locales.map { Locale.forLanguageTag(it.locale) }
    }

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...)
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found
     *
     * See [LocalizationFunction] on how your command localization keys need to be constructed
     *
     * See [DefaultLocalizationMapProvider] for default implementation details
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
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
        override val applicationFilters = this@BApplicationConfigBuilder.applicationFilters.toImmutableList()
        override val baseNameToLocalesMap =
            this@BApplicationConfigBuilder.baseNameToLocalesMap.mapValues { (_, v) -> v.toImmutableList() }
                .toImmutableMap()
    }
}