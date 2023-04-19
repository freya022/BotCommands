package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.ApplicationCommandParameter
import kotlin.reflect.KParameter

abstract class AbstractSlashCommandParameter(
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder,
    val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandParameter(parameter, optionBuilder) {
    val varArgs = optionBuilder.varArgs
    private val numRequired = optionBuilder.requiredVarArgs
    val isVarArg: Boolean
        get() = varArgs != -1

    fun isRequiredVararg(varArgNum: Int): Boolean {
        return when {
            !isVarArg -> !isOptional //Default if not a vararg
            else -> varArgNum < numRequired
        }
    }
}