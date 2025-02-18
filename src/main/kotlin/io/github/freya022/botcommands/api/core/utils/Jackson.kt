package io.github.freya022.botcommands.api.core.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

/**
 * Creates a [TypeReference] of type [T].
 */
@Deprecated(
    message = "Replaced by jacksonTypeRef",
    replaceWith = ReplaceWith("jacksonTypeRef<T>()", "com.fasterxml.jackson.module.kotlin.jacksonTypeRef")
)
inline fun <reified T : Any> typeReferenceOf() = jacksonTypeRef<T>()