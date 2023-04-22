package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.ApplicationCommandParameter
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isJava
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

abstract class AbstractSlashCommandParameter(
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder,
    val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandParameter(parameter, optionBuilder) {
    val varArgs = optionBuilder.varArgs
    private val numRequired = optionBuilder.requiredVarArgs
    val isVarArg: Boolean
        get() = varArgs != -1

    init {
        if (isVarArg) {
            requireUser(kParameter.type.jvmErasure == List::class, kParameter.function) {
                "Type of vararg options should be List"
            }

            if (varArgs != numRequired) {
                if (parameter.collectionElementType?.isMarkedNullable == false && !parameter.function.isJava) {
                    throwUser(parameter.function, "List element type should be nullable on vararg parameters where not all arguments are required")
                }
            }
        }
    }

    fun isRequiredVararg(varArgNum: Int): Boolean {
        return when {
            !isVarArg -> !isOptional //Default if not a vararg
            else -> varArgNum < numRequired
        }
    }
}