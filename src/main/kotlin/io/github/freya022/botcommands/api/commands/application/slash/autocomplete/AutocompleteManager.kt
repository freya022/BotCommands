package io.github.freya022.botcommands.api.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.BContext
import kotlin.reflect.KFunction

interface AutocompleteManager {
    val context: BContext

    fun autocomplete(name: String, function: KFunction<Collection<Any>>, block: AutocompleteInfoBuilder.() -> Unit)
}