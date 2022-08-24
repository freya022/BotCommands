package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignatureNoSrc

@BService
internal class AutocompleteHandlerContainer {
    private val autocompleteHandlers: MutableMap<String, AutocompleteHandler> = hashMapOf()

    operator fun get(handlerName: String): AutocompleteHandler? = autocompleteHandlers[handlerName]

    operator fun plusAssign(handler: AutocompleteHandler) {
        autocompleteHandlers[handler.autocompleteInfo.name]?.let {
            if (it.autocompleteInfo == handler.autocompleteInfo) {
                return //Skip assignation & exception if both autocompletes are the exact same
            }

            throwUser(
                """
                Tried to add an autocomplete handler with the same name, but with different characteristics: '${handler.autocompleteInfo.name}'
                Old function: ${it.autocompleteInfo.method.shortSignatureNoSrc}
                New function: ${handler.autocompleteInfo.method.shortSignatureNoSrc}""".trimIndent()
            )
        }

        autocompleteHandlers[handler.autocompleteInfo.name] = handler
    }
}