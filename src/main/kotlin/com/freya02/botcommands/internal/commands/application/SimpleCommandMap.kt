package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import java.util.*
import kotlin.reflect.KFunction

internal class SimpleCommandMap<T>(private val nameSupplier: (T) -> String, private val functionSupplier: ((T) -> KFunction<*>)?) {
    private val mutableMap: MutableMap<String, T> = hashMapOf()
    val map: Map<String, T>
        get() = Collections.unmodifiableMap(mutableMap)

    fun putNewCommand(newCommand: T) {
        mutableMap.putIfAbsent(newCommand.let(nameSupplier), newCommand)?.let { oldCommand ->
            throwUser( //TODO implement INamedCommandInfo into builders, will replace name supplier
                """
                Command '${newCommand.let(nameSupplier)}' is already defined
                Existing command: ${functionSupplier?.invoke(oldCommand)?.shortSignature}
                Current command: ${functionSupplier?.invoke(newCommand)?.shortSignature}
                """.trimIndent()
            )
        }
    }

    fun isEmpty(): Boolean = mutableMap.isEmpty()

    companion object {
        fun <T> ofBuilders(): SimpleCommandMap<T> where T : CommandBuilder,
                                                        T : IBuilderFunctionHolder<*> {
            return SimpleCommandMap(CommandBuilder::name, IBuilderFunctionHolder<*>::function)
        }

        fun <T> ofInfos(): SimpleCommandMap<T> where T : AbstractCommandInfo,
                                                     T : IExecutableInteractionInfo {
            return SimpleCommandMap({ it.path.fullPath }, IExecutableInteractionInfo::method)
        }
    }
}