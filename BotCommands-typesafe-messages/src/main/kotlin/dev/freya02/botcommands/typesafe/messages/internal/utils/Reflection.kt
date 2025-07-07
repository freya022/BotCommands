package dev.freya02.botcommands.typesafe.messages.internal.utils

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

internal fun Method.isAbstract(): Boolean {
    return (modifiers and Modifier.ABSTRACT) == Modifier.ABSTRACT
}

internal val KClass<*>.simpleNestedBinaryName: String
    get() = simpleNestedName.replace('.', '$')
