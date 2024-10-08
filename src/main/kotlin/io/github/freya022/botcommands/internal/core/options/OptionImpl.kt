package io.github.freya022.botcommands.internal.core.options

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.utils.isPrimitive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.options.builder.CommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal abstract class OptionImpl private constructor(
    internal val optionParameter: OptionParameter,
    internal val optionType: OptionType,
    /** @see CommandOptionBuilderImpl.isOptional */
    optional: Boolean?
) : Option {
    internal constructor(optionImpl: OptionImpl) : this(optionImpl.optionParameter, optionImpl.optionType, optionImpl.isOptional)
    internal constructor(optionParameter: OptionParameter, optionType: OptionType) : this(optionParameter, optionType, null)

    internal constructor(commandOptionBuilder: CommandOptionBuilderImpl) : this(
        commandOptionBuilder.optionParameter,
        OptionType.OPTION,
        commandOptionBuilder.isOptional
    )

    internal val typeCheckingFunction: KFunction<*>
        get() = optionParameter.typeCheckingFunction

    /**
     * This might be a KParameter from the command function or from an aggregate function.
     *
     * - In the case the option was created from [the internal aggregator][InternalAggregators.theSingleAggregator], the KParameter is grabbed from the command function.
     * - In the case the option was created in a user-defined aggregator, then the KParameter is grabbed from the aggregate function.
     *
     * **Beware of the types and nullabilities**, the KParameter could be of an array type.
     *
     * **Also note:** this is not unique, multiple options can be bound to the same KParameter. (varargs for example)
     */
    final override val kParameter: KParameter
        get() = optionParameter.typeCheckingParameter
    /**
     * Parameter used solely when inserting the option in the aggregation function.
     *
     * May come from the command or aggregate function.
     */
    internal val executableParameter: KParameter
        get() = optionParameter.executableParameter

    final override val type = kParameter.type
    final override val isOptional: Boolean = optional ?: kParameter.isOptional
    final override val isNullable: Boolean = kParameter.isNullable
    final override val isVararg: Boolean
        get() = optionParameter.executableFunction.isVarargAggregator()
    final override val declaredName: String
        get() = optionParameter.typeCheckingParameterName
    final override val index
        get() = kParameter.index
    final override val nullValue = when {
        isNullable -> null
        kParameter.isPrimitive -> primitiveDefaultValue(type.jvmErasure)
        else -> null
    }
}

private val primitiveDefaultValues: Map<KClass<*>, *> = mapOf(
    Byte::class to 0.toByte(),
    Short::class to 0.toShort(),
    Int::class to 0,
    Long::class to 0L,
    Float::class to 0.0F,
    Double::class to 0.0,
    Boolean::class to false
)

private fun primitiveDefaultValue(clazz: KClass<*>) =
    primitiveDefaultValues[clazz] ?: throwInternal("No primitive default value for ${clazz.simpleNestedName}")