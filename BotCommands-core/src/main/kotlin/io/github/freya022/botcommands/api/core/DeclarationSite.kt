package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import io.github.freya022.botcommands.internal.utils.shortSignature
import kotlin.reflect.KFunction

/**
 * Represents a place where something (usually commands) was declared, used in exception messages.
 *
 * When creating code-declared commands, the declaration site is automatically set to the caller.
 * As this is done by walking the stack, you can use [@IgnoreStackFrame][IgnoreStackFrame]
 * to ignore a particular class (such as utility classes), pointing instead to the frame which called your ignored class.
 */
class DeclarationSite private constructor(
    // TODO shared internal
    /** INTERNAL */
    @get:JvmSynthetic
    val string: String
) {
    override fun toString(): String = string

    companion object {
        @JvmStatic
        @SkipJavaReflectionOverload("Expected to be supplied with an Executable's function")
        fun fromFunctionSignature(function: KFunction<*>): DeclarationSite {
            return DeclarationSite(function.shortSignature)
        }

        @JvmStatic
        fun fromRaw(string: String) = DeclarationSite(string)
    }
}
