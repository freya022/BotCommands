package io.github.freya022.botcommands.internal.commands.text.autobuilder.utils

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.text.autobuilder.metadata.TextFunctionMetadata
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import kotlin.reflect.KFunction

internal class TextCommandVariationContainer {
    private val _list: MutableList<TextFunctionMetadata> = arrayListOf()
    val asList: List<TextFunctionMetadata> get() = _list

    fun firstOrNull(): TextFunctionMetadata? = asList.firstOrNull()

    fun addVariation(metadata: TextFunctionMetadata) {
        // Text command variations are required to all be from the same class
        asList.firstOrNull()?.let {
            check(it.declaringClass == metadata.declaringClass) {
                "All variations of text command '${metadata.path}' must be in the same class"
            }
        }
        _list.add(metadata)
    }

    /**
     * Checks all variations and declaring class
     */
    context(_: CommandBuilder)
    inline fun <reified A : Annotation> hasAnyAnnotationOf(): Boolean {
        return singleAnnotationOfVariants<A>() != null
    }

    /**
     * Checks all variations and declaring class
     */
    context(_: CommandBuilder)
    inline fun <reified A : Annotation> singleAnnotationOfVariants(): A? {
        return singleValueOfVariants(annotationRef<A>()) { it.findAnnotationRecursive<A>() }
            ?: firstOrNull()?.declaringClass?.findAnnotationRecursive<A>()
    }

    context(builder: CommandBuilder)
    fun <V : Any> singleValueOfVariants(desc: String, associationBlock: (KFunction<*>) -> V?): V? {
        val allValues = asList.map { it.func }.associateWith(associationBlock)

        // Check only a single variant has the annotation
        val nonNullMap = allValues.filterValues { it != null }
        check(nonNullMap.size <= 1) {
            val refs = nonNullMap.keys.joinAsList { it.shortSignature }
            "Command '${builder.path}' should have $desc defined at most once:\n$refs"
        }

        return nonNullMap.values.firstOrNull()
    }
}
