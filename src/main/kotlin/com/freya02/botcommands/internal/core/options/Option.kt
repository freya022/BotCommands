package com.freya02.botcommands.internal.core.options

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isVarargAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.isPrimitive
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

enum class OptionType {
    OPTION,
    CUSTOM,
    CONSTANT, //TODO
    GENERATED
}

interface Option {
    val optionParameter: OptionParameter
    val optionType: OptionType

    /**
     * This might be a KParameter from the command function or from an aggregate function.
     *
     * - In the case the option was created from [the internal aggregator][OptionAggregateBuildersImpl.theSingleAggregator], the KParameter is grabbed from the command function.
     * - In the case the option was created in a user-defined aggregator, then the KParameter is grabbed from the aggregate function.
     *
     * **Beware of the types and nullabilities**, the KParameter could be of an array type.
     *
     * **Also note:** this is not unique, multiple options can be bound to the same KParameter. (varargs for example)
     */
    val kParameter: KParameter

    /**
     * Parameter used solely when inserting the option in the aggregator function.
     *
     * May be from the command or aggregate function.
     */
    val executableParameter: KParameter

    val isVararg: Boolean
    val isOptional: Boolean
    val declaredName: String
    val index: Int
    val isPrimitive: Boolean
    val type: KType
}

open class OptionImpl(
    final override val optionParameter: OptionParameter,
    final override val optionType: OptionType
) : Option {
    final override val kParameter: KParameter
        get() = optionParameter.typeCheckingParameter
    final override val executableParameter: KParameter
        get() = optionParameter.executableParameter

    final override val type = kParameter.type
    final override val isOptional by lazy { kParameter.isNullable || kParameter.isOptional }
    final override val isVararg = optionParameter.executableFunction.isVarargAggregator() && type.jvmErasure == List::class
    final override val declaredName = kParameter.findDeclarationName()
    final override val index = kParameter.index
    final override val isPrimitive = kParameter.isPrimitive
}