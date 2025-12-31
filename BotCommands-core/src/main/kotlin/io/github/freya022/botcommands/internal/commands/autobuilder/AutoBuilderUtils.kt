package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.rethrow
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.unwrap
import kotlin.reflect.KFunction

//This is used so commands can't prevent other commands from being registered when an exception happens
internal inline fun <T : MetadataFunctionHolder> Iterable<T>.forEachWithDelayedExceptions(crossinline block: (T) -> Unit) {
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

context(_: CommandBuilder)
internal inline fun <reified A : Annotation> Iterable<KFunction<*>>.singlePresentAnnotationOfVariants(): Boolean {
    return singleAnnotationOfVariants<A>() != null
}

context(_: CommandBuilder)
internal inline fun <reified A : Annotation> Iterable<KFunction<*>>.singleAnnotationOfVariants(): A? {
    return singleValueOfVariants(annotationRef<A>()) { it.findAnnotationRecursive<A>() }
}

context(builder: CommandBuilder)
internal fun <V : Any> Iterable<KFunction<*>>.singleValueOfVariants(desc: String, associationBlock: (KFunction<*>) -> V?): V? {
    val allValues = this.associateWith(associationBlock)

    val nonNullMap = allValues.filterValues { it != null }
    check(nonNullMap.size <= 1) {
        val refs = nonNullMap.map { it.key }.joinAsList { it.shortSignature }
        "Command '${builder.path}' should have $desc defined at most once:\n$refs"
    }
    return nonNullMap.values.firstOrNull()
}

@Suppress("UNCHECKED_CAST")
internal fun KFunction<*>.castFunction() = this as KFunction<Any>
