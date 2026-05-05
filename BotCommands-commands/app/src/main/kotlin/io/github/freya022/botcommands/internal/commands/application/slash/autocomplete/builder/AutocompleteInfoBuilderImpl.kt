package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.cache.factory.AbstractAutocompleteCacheFactory
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.AutocompleteCacheFactory
import io.github.freya022.botcommands.internal.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KFunction

internal class AutocompleteInfoBuilderImpl internal constructor(
    private val context: BContext,
    internal val name: String?,
    override val function: KFunction<Collection<Any>>,
) : AutocompleteInfoBuilder,
    IBuilderFunctionHolder<Collection<*>> {

    override lateinit var declarationSite: DeclarationSite

    override var mode: AutocompleteMode = AutocompleteMode.FUZZY

    override var showUserInput: Boolean = false

    internal var autocompleteCacheFactory: AbstractAutocompleteCacheFactory? = null
        private set

    override fun cache(factory: AutocompleteCacheFactory) {
        requireAt(autocompleteCacheFactory == null, declarationSite) {
            "Autocomplete cache was already initialized!"
        }

        autocompleteCacheFactory = factory as? AbstractAutocompleteCacheFactory
            ?: error("Custom autocomplete caches are not supported yet")
    }

    internal fun build(): AutocompleteInfoImpl {
        return AutocompleteInfoImpl(context, this)
    }
}
