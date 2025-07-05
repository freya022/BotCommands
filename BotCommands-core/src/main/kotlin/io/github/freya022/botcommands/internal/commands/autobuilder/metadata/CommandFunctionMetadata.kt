package io.github.freya022.botcommands.internal.commands.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KClass

internal abstract class CommandFunctionMetadata<T : Any, A : Annotation>(
    private val classPathFunction: ClassPathFunction,
    instanceType: KClass<T>,
    val annotation: A,
    val path: CommandPath
) : MetadataFunctionHolder {
    final override val func get() = classPathFunction.function

    init {
        requireAt(classPathFunction.clazz.isSubclassOf(instanceType), func) {
            "Declaring class must extend ${instanceType.simpleName}"
        }
    }

    @Suppress("UNCHECKED_CAST")
    val instance: T
        get() = classPathFunction.instance as T
}