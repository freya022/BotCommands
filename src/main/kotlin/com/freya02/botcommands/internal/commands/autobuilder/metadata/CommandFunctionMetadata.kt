package com.freya02.botcommands.internal.commands.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal abstract class CommandFunctionMetadata<T : Any, A : Annotation>(
    private val classPathFunction: ClassPathFunction,
    private val instanceType: KClass<T>,
    val annotation: A,
    val path: CommandPath
) {
    val func get() = classPathFunction.function

    val instance: T
        get() = instanceType.safeCast(classPathFunction.instance)
            ?: throwUser(func, "Declaring class must extend ${instanceType.simpleName}")
}