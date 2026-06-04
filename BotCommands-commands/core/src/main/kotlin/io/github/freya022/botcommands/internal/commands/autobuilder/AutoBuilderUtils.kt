package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder
import io.github.freya022.botcommands.internal.utils.rethrow
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.unwrap
import kotlin.reflect.KFunction

//This is used so commands can't prevent other commands from being registered when an exception happens
inline fun <T : MetadataFunctionHolder> Iterable<T>.forEachWithDelayedExceptions(crossinline block: (T) -> Unit) {
    var ex: Throwable? = null
    forEach { metadata ->
        runCatching {
            block(metadata)
        }.onFailure {
            val newException = RuntimeException("An exception occurred while processing function ${metadata.func.shortSignature}", it.unwrap())
            if (ex == null) {
                ex = newException
            } else {
                ex.addSuppressed(newException)
            }
        }
    }

    ex?.rethrow("Exception(s) occurred while registering annotated commands")
}

@Suppress("UNCHECKED_CAST")
fun KFunction<*>.castFunction() = this as KFunction<Any>
