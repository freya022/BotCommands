package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.ResolverContainer
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

object CommandOptions {
    internal inline fun <reified T : CommandOptionBuilder, reified R : Any> transform(
        context: BContextImpl,
        options: Map<String, List<OptionBuilder>>,
        config: Configuration<T, R>
    ): List<AbstractOption> {
        val resolverContainer = context.getService<ResolverContainer>()

        return options.values.flatten().map { optionBuilder ->
            val kParameter = optionBuilder.parameter
            return@map when (optionBuilder) {
                is T -> {
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
                is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter(kParameter)
                else -> throwInternal("Unsupported option builder: $optionBuilder")
            }
        }
    }

    interface Configuration<T, R> {
        fun transformOption(optionBuilder: T, resolver: R): AbstractOption =
            throwInternal("This should have not been called")
    }
}