package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import kotlin.reflect.KFunction

@BService
@RequiresApplicationCommands
internal class AutocompleteInfoContainer internal constructor() {
    private val infoByName: MutableMap<String, AutocompleteInfoImpl> = hashMapOf()
    private val infoByFunction: MutableMap<KFunction<Collection<Any>>, AutocompleteInfoImpl> = hashMapOf()

    internal val allInfos get() = infoByFunction.values
    internal val size get() = infoByFunction.size

    internal operator fun plusAssign(autocompleteInfo: AutocompleteInfoImpl) {
        autocompleteInfo.name?.let { name ->
            infoByName.putIfAbsentOrThrow(name, autocompleteInfo) {
                "Autocomplete handler '$name' is already registered:\nAt: ${autocompleteInfo.declarationSite}\nAnd: ${it.declarationSite}"
            }
        }
        infoByFunction.putIfAbsentOrThrow(autocompleteInfo.function, autocompleteInfo) {
            "Autocomplete handler is already registered:\nAt: ${autocompleteInfo.declarationSite}\nAnd: ${it.declarationSite}"
        }
    }

    internal operator fun get(handlerName: String): AutocompleteInfoImpl? = infoByName[handlerName]
    internal operator fun get(handlerFunction: KFunction<Collection<Any>>): AutocompleteInfoImpl? = infoByFunction[handlerFunction.reflectReference()]
}
