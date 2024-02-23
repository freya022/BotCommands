package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import kotlin.reflect.KFunction

interface AutocompleteManager {
    val context: BContext

    fun autocomplete(function: KFunction<Collection<Any>>, name: String? = null, block: AutocompleteInfoBuilder.() -> Unit)
}