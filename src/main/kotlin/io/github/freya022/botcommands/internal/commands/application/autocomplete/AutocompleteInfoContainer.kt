package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoImpl
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc

@BService
internal class AutocompleteInfoContainer {
    private val infoMap: MutableMap<String, AutocompleteInfoImpl> = hashMapOf()

    operator fun plusAssign(autocompleteInfo: AutocompleteInfoImpl) {
        infoMap.putIfAbsentOrThrow(autocompleteInfo.name, autocompleteInfo) {
            "Autocomplete handler ${autocompleteInfo.name} is already registered at ${autocompleteInfo.function.shortSignatureNoSrc} and at ${it.function.shortSignatureNoSrc}"
        }
    }

    operator fun get(handlerName: String): AutocompleteInfoImpl? = infoMap[handlerName]
}
