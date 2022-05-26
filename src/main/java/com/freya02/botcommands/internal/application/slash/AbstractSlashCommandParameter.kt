package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.internal.application.ApplicationCommandParameter
import kotlin.reflect.KParameter

abstract class AbstractSlashCommandParameter(
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder
) : ApplicationCommandParameter(parameter, optionBuilder) {
    val varArgs: Int
    private val numRequired: Int
    val isVarArg: Boolean
        get() = varArgs != -1

    init {
        varArgs = -1 //TODO option builder
        numRequired = 0 //TODO option builder
    }

    fun isRequiredVararg(varArgNum: Int): Boolean {
        return when {
            !isVarArg -> !isOptional //Default if not a vararg
            else -> varArgNum < numRequired
        }
    }
}