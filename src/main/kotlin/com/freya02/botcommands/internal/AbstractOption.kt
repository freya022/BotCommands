package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.application.IApplicationCommandManager
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.parameters.MultiParameter
import kotlin.reflect.KParameter
import kotlin.reflect.KType

interface AbstractOption {
    val multiParameter: MultiParameter

    /**
     * This might be a KParameter from the command function or from an aggregate function
     *
     * - In the case the option was created from a (Type)CommandBuilder aggregate, the KParameter is grabbed from the command function, set in [IApplicationCommandManager.slashCommand]
     *      - This means the KFunction is mandatory, and must be passed to the command declaration (before the DSL is executed)
     * - In the case the option was created in a user-defined aggregate, then the KParameter is grabbed from the aggregate function
     *
     * **Beware of the types and nullabilities**, the KParameter could be of an array type, which should be supported at a reason of 1 per aggregate
     *
     * **Also note:** this is not unique, multiple options can be bound to the same KParameter (varargs for example)
     */
    val kParameter: KParameter
        get() = multiParameter.typeCheckingParameter

    /**
     * Parameter used solely when inserting the option in the aggregator function.
     *
     * May be from the command or aggregate function.
     */
    val executableParameter: KParameter
        get() = multiParameter.executableParameter
    val methodParameterType: MethodParameterType

    //TODO move all of those methods in some object, where the values are stored
    val isVararg: Boolean
        get() = kParameter.isVararg
    val isOptional: Boolean
    val declaredName: String
        get() = kParameter.findDeclarationName()
    val index: Int
        get() = kParameter.index
    val isPrimitive: Boolean
        get() = kParameter.isPrimitive
    val type: KType
        get() = kParameter.type
}
