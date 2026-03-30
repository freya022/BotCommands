package io.github.freya022.botcommands.internal.core.reflection

import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.isConstructor
import io.github.freya022.botcommands.api.core.utils.isStatic
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.core.method.accessors.MethodAccessorFactoryProvider
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSingleAggregator
import io.github.freya022.botcommands.internal.core.service.getFunctionServiceOrNull
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.checkAt
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

class AggregatorFunction private constructor(
    boundAggregator: KFunction<*>,
    /**
     * Nullable due to constructor aggregators
     */
    aggregatorInstance: Any?,
    firstParamType: KClass<*>
) : Function<Any?>(boundAggregator) {
    init {
        // Check that the function is not static
        // as we don't have any reflection metadata for those.
        // Constructors and value class "constructors" are static, ignore those.
        if (!aggregator.isConstructor && !aggregator.declaringClass.isValue) {
            checkAt(!aggregator.isStatic, aggregator) {
                "Non-constructor aggregators must be in a service and cannot be static"
            }
        }
    }

    private val hasEventParameter =
        aggregator.nonInstanceParameters.first().type.jvmErasure.isSubclassOf(firstParamType)

    internal val methodAccessor = MethodAccessorFactoryProvider.getAccessorFactory().create(aggregatorInstance, kFunction)
    internal val aggregator get() = this.kFunction

    internal val isSingleAggregator get() = aggregator.isSingleAggregator()

    internal constructor(
        context: BContext,
        aggregator: KFunction<*>,
        firstParamType: KClass<*>
    ) : this(aggregator, context.serviceContainer.getFunctionServiceOrNull(aggregator), firstParamType)

    internal suspend fun aggregate(firstParam: Any, aggregatorArguments: MethodArguments): Any? {
        if (hasEventParameter) {
            aggregatorArguments[0] = firstParam
        }

        return methodAccessor.callSuspend(aggregatorArguments)
    }
}

fun KFunction<*>.toAggregatorFunction(context: BContext, firstParamType: KClass<*>) =
    AggregatorFunction(context, this, firstParamType)
