package io.github.freya022.botcommands.api.core.utils

import com.fasterxml.jackson.core.type.TypeReference

/**
 * Creates a [TypeReference] of type [T].
 */
inline fun <reified T : Any> typeReferenceOf() = object : TypeReference<T>() {}