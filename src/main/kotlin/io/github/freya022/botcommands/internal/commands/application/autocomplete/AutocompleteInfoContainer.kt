package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import kotlin.reflect.KFunction

@BService
internal class AutocompleteInfoContainer internal constructor() {
    private val infoByName: MutableMap<String, AutocompleteInfoImpl> = hashMapOf()
    private val infoByFunction: MutableMap<KFunction<*>, AutocompleteInfoImpl> = hashMapOf()

    internal val allInfos get() = infoByFunction.values
    internal val size get() = infoByFunction.size

    internal operator fun plusAssign(autocompleteInfo: AutocompleteInfoImpl) {
        autocompleteInfo.name?.let { name ->
            infoByName.putIfAbsentOrThrow(name, autocompleteInfo) {
                "Autocomplete handler '$name' is already registered at ${autocompleteInfo.function.shortSignatureNoSrc} and at ${it.function.shortSignatureNoSrc}"
            }
        }
        infoByFunction.putIfAbsentOrThrow(autocompleteInfo.function, autocompleteInfo) {
            "Autocomplete handler is already registered at ${autocompleteInfo.function.shortSignatureNoSrc} and at ${it.function.shortSignatureNoSrc}"
        }
    }

    internal operator fun get(handlerName: String): AutocompleteInfoImpl? = infoByName[handlerName]
    internal operator fun get(handlerFunction: KFunction<*>): AutocompleteInfoImpl? = infoByFunction[handlerFunction.reflectReference()]
}
