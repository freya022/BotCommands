package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.autobuilder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteCacheKey
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CaffeineAutocompleteCache
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.ForceAutocompleteCache
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.caffeineCache
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoAutoBuilder
import kotlin.reflect.KFunction

internal class CaffeineAutocompleteCacheAutoBuilder : AutocompleteInfoAutoBuilder.CacheBuilder {

    context(builder: AutocompleteInfoBuilder)
    override fun handle(function: KFunction<Collection<Any>>): Boolean {
        val annotation = function.findAnnotationRecursive<CaffeineAutocompleteCache>() ?: return false

        builder.caffeineCache {
            forceCache = function.hasAnnotationRecursive<ForceAutocompleteCache>()
            cacheSize = annotation.cacheSize

            val cacheKey = function.findAnnotationRecursive<AutocompleteCacheKey>() ?: return@caffeineCache
            compositeKeys = cacheKey.compositeKeys.toList()
            userLocal = cacheKey.userLocal
            channelLocal = cacheKey.channelLocal
            guildLocal = cacheKey.guildLocal
        }

        return true
    }
}
