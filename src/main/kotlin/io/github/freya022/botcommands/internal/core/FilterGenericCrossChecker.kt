package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandRejectionHandler
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.superErasureAt
import kotlin.reflect.jvm.jvmErasure

@BService(priority = Int.MAX_VALUE - 1)
internal class FilterGenericCrossChecker internal constructor(context: BContext) {
    init {
        checkTypes<TextCommandFilter<*>, TextCommandRejectionHandler<*>>(context)
        checkTypes<ApplicationCommandFilter<*>, ApplicationCommandRejectionHandler<*>>(context)
        checkTypes<ComponentInteractionFilter<*>, ComponentInteractionRejectionHandler<*>>(context)
    }

    private inline fun <reified F : Filter, reified H : Any> checkTypes(context: BContext) {
        val filterTypes = context.getInterfacedServiceTypes<F>()
        if (filterTypes.isEmpty()) {
            // Found a rejection handler but no filter
            return
        }

        val rejectionHandlerType = context.getInterfacedServiceTypes<H>().firstOrNull() ?: return

        val filterOutputTypes = filterTypes.map { it.superErasureAt<F>(0).jvmErasure }
        check(filterOutputTypes.all { filterOutputTypes.first() == it }) {
            "All ${classRef<F>()} must have the same return type, current types: ${filterOutputTypes.joinToString { it.simpleNestedName }}"
        }

        val rejectionHandlerInputType = rejectionHandlerType.superErasureAt<H>(0).jvmErasure
        check(rejectionHandlerInputType.isAssignableFrom(filterOutputTypes.first())) {
            val inputName = rejectionHandlerInputType.simpleNestedName
            val outputName = filterOutputTypes.first().simpleNestedName
            "Input type of ${classRef<H>()} ($inputName) must be the same (or a superclass) as the output type of ${classRef<F>()} ($outputName)"
        }
    }
}