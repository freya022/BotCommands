package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwUser
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService
internal class AutocompleteHandlerContainer {
    private val handlersLock = ReentrantLock()
    private val autocompleteHandlers: MutableMap<String, AutocompleteHandler> = hashMapOf()

    operator fun get(handlerName: String): AutocompleteHandler? = handlersLock.withLock { autocompleteHandlers[handlerName] }

    operator fun plusAssign(handler: AutocompleteHandler) = handlersLock.withLock {
        autocompleteHandlers[handler.autocompleteInfo.name]?.let {
            if (it.autocompleteInfo == handler.autocompleteInfo) {
                return //Skip assignation & exception if both autocompletes are the exact same
            }

            throwUser(
                """
                Tried to add an autocomplete handler with the same name, but with different characteristics: '${handler.autocompleteInfo.name}'
                Old function: ${it.autocompleteInfo.function.shortSignatureNoSrc}
                New function: ${handler.autocompleteInfo.function.shortSignatureNoSrc}""".trimIndent()
            )
        }

        autocompleteHandlers[handler.autocompleteInfo.name] = handler
    }
}