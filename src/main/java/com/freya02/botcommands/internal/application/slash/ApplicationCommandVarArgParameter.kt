package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.internal.application.ApplicationCommandParameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

abstract class ApplicationCommandVarArgParameter<RESOLVER : Any>(
    resolverType: KClass<RESOLVER>,
    parameter: KParameter,
    index: Int
) : ApplicationCommandParameter<RESOLVER>(
    resolverType, parameter, parameter.type, index
) {
    val varArgs = -1
    private val numRequired = 0 //TODO varargs
    val isVarArg = varArgs != -1

    fun isRequiredVararg(varArgNum: Int): Boolean {
        return when {
            !isVarArg -> !isOptional //Default if not a vararg
            else -> varArgNum < numRequired
        }
    }
}