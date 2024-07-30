package io.github.freya022.botcommands.internal.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.builder.ExecutableCommandBuilderImpl
import kotlin.reflect.KFunction

internal abstract class ApplicationCommandBuilderImpl<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilderImpl<T, Any>(context, name, function),
    ApplicationCommandBuilder<T> {

    final override val filters: MutableList<ApplicationCommandFilter<*>> = arrayListOf()

    final override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }
}