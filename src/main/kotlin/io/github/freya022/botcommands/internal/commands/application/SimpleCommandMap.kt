package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwUser
import java.util.*
import kotlin.reflect.KFunction

internal class SimpleCommandMap<T : INamedCommand>(private val functionSupplier: ((T) -> KFunction<*>)?) {
    private val mutableMap: MutableMap<String, T> = hashMapOf()
    val map: Map<String, T>
        get() = Collections.unmodifiableMap(mutableMap)

    fun putNewCommand(newCommand: T) {
        mutableMap.putIfAbsent(newCommand.name, newCommand)?.let { oldCommand ->
            throwUser(
                """
                Command '${newCommand.path.fullPath}' is already defined
                Existing command: ${functionSupplier?.invoke(oldCommand)?.shortSignature}
                Current command: ${functionSupplier?.invoke(newCommand)?.shortSignature}
                """.trimIndent()
            )
        }
    }

    fun isEmpty(): Boolean = mutableMap.isEmpty()

    companion object {
        fun <T> ofBuilders(): SimpleCommandMap<T> where T : INamedCommand,
                                                        T : IBuilderFunctionHolder<*> {
            return SimpleCommandMap(IBuilderFunctionHolder<*>::function)
        }

        fun <T> ofInfos(): SimpleCommandMap<T> where T : INamedCommand,
                                                     T : IExecutableInteractionInfo {
            return SimpleCommandMap(IExecutableInteractionInfo::function)
        }
    }
}