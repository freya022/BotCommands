package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.ResolverContainer
import kotlin.reflect.full.createType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

object CommandOptions {
    internal inline fun <reified T : CommandOptionBuilder, reified R : Any> transform(
        context: BContextImpl,
        aggregateBuilder: OptionAggregateBuilder,
        config: Configuration<T, R>
    ): List<AbstractOption> {
        val aggregator = aggregateBuilder.aggregator
        val options = aggregateBuilder.optionBuilders
        val resolverContainer = context.getService<ResolverContainer>()

        requireUser(options.size != aggregator.valueParameters.size, aggregator) {
            "Aggregator should have the same number of parameters as there is options, found ${options.size} options and ${aggregator.valueParameters.size} parameters"
        }

        return options.values.flatten().map { optionBuilder ->
            when (optionBuilder) {
                is T -> {
                    val kParameter = optionBuilder.parameter
                    val parameter = when {
                        kParameter.isVararg -> {
                            val type = kParameter.type
                            //kotlin moment
                            val elementsType = type.jvmErasure.java.componentType.kotlin
                                .createType(type.arguments, type.isMarkedNullable, type.annotations)
                            kParameter.wrap().copy(type = elementsType)
                        }

                        else -> kParameter.wrap()
                    }

                    when (val resolver = resolverContainer.getResolver(parameter)) {
                        is R -> config.transformOption(optionBuilder, resolver)
                        else -> throwUser(
                            optionBuilder.owner,
                            "Expected a resolver of type ${R::class.simpleNestedName} but ${resolver.javaClass.simpleNestedName} does not support it"
                        )
                    }
                }
                is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter()
                is CustomOptionBuilder -> TODO()
                else -> throwInternal("Unsupported option builder: $optionBuilder")
            }
        }
    }

    interface Configuration<T, R> {
        fun transformOption(optionBuilder: T, resolver: R): AbstractOption =
            throwInternal("This should have not been called")
    }
}