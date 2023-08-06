package com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations

import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.core.config.BConfigBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel

/**
 * Enables autocomplete caching.
 *
 * By default, this will cache results by key, which is the input of the focused option.<br>
 * However, you can use composite keys if you want to cache based off multiple option values,
 * see [compositeKeys] for more details.
 *
 * @see SlashOption @SlashOption
 * @see AutocompleteHandler @AutocompleteHandler
 *
 * @see AutocompleteInfoBuilder.cache DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheAutocomplete(
    /**
     * Sets the [autocomplete cache mode][AutocompleteCacheMode].
     *
     * Tip: You can mark app options your autocomplete depends on as composite keys,
     * this would be useful to make an autocomplete result depend on multiple options,
     * instead of only the focused one.
     */
    val cacheMode: AutocompleteCacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY,

    /**
     * Whether the cache should be used even if [autocomplete cache is disabled][BConfigBuilder.disableAutocompleteCache].<br>
     * This could be useful if your autocomplete is heavy even in a development environment.
     *
     * @return `true` if the autocomplete results should be cached anyway
     *
     * @see BConfigBuilder.disableAutocompleteCache
     * @see AutocompleteCacheInfoBuilder.forceCache
     */
    val forceCache: Boolean = false,

    /**
     * Sets the cache size for this autocomplete cache, **in kilobytes (KB)**.
     *
     * **Note:** This only takes effect with [AutocompleteCacheMode.CONSTANT_BY_KEY].
     *
     * @see AutocompleteCacheInfoBuilder.cacheSize
     */
    val cacheSize: Long = 2048,

    /**
     * The set of **option names** (the one you see on Discord) which forms the cache key.
     *
     * This could be useful when making an autocomplete which depends on multiple options.
     *
     * **Note:** The focused option will always be in the composite key.<br>
     * **Note 2:** Parameter names also work, but will not work in case the parameter is a vararg,
     * in which case you must use the option names.
     *
     * @see AutocompleteCacheInfoBuilder.compositeKeys
     */
    val compositeKeys: Array<String> = [],

    /**
     * Defines whether this autocomplete will give different results based on which [Guild] this interaction is executing on.
     *
     * @return `true` if the autocomplete depends on the [Guild] this interaction is execution on
     *
     * @see AutocompleteCacheInfoBuilder.guildLocal
     */
    val guildLocal: Boolean = false,

    /**
     * Defines whether this autocomplete will give different results based on which [User] is executing this interaction.
     *
     * @return `true` if the autocomplete depends on which [User] is executing this interaction
     *
     * @see AutocompleteCacheInfoBuilder.userLocal
     */
    val userLocal: Boolean = false,

    /**
     * Defines whether this autocomplete will give different results based on which [Channel] this interaction is executing on.
     *
     * @return `true` if the autocomplete depends on the [Channel] this interaction is execution on
     *
     * @see AutocompleteCacheInfoBuilder.channelLocal
     */
    val channelLocal: Boolean = false
)