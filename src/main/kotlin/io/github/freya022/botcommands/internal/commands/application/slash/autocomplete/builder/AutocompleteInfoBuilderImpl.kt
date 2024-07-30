package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.builder

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteCacheInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteCacheInfo
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoImpl
import io.github.freya022.botcommands.internal.commands.builder.IBuilderFunctionHolder
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

    internal var autocompleteCache: AutocompleteCacheInfo? = null
        private set

    override fun cache(block: AutocompleteCacheInfoBuilder.() -> Unit) {
        autocompleteCache = AutocompleteCacheInfoBuilderImpl().apply(block).build()
    }

    @Deprecated(
        message = "Only had one mode ever, that always has been and will still be the default",
        ReplaceWith("cache(block)")
    )
    override fun cache(cacheMode: AutocompleteCacheMode, block: AutocompleteCacheInfoBuilder.() -> Unit) {
        autocompleteCache = AutocompleteCacheInfoBuilderImpl().apply(block).build()
    }

    internal fun build(): AutocompleteInfoImpl {
        return AutocompleteInfoImpl(context, this)
    }
}