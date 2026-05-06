package io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations

import io.github.freya022.botcommands.api.core.config.BApplicationConfig

/**
 * Forces the cache to be used even if [autocomplete cache is disabled][BApplicationConfig.disableAutocompleteCache].
 *
 * This could be useful if your autocomplete is heavy even in a development environment.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ForceAutocompleteCache
