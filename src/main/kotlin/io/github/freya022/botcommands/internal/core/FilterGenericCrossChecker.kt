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
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.classRef
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure
import kotlin.system.exitProcess

@BService(priority = Int.MAX_VALUE - 1)
class FilterGenericCrossChecker(context: BContext) {
    init {
        checkTypes<TextCommandFilter<*>, TextCommandRejectionHandler<*>>(context)
        checkTypes<ApplicationCommandFilter<*>, ApplicationCommandRejectionHandler<*>>(context)
        checkTypes<ComponentInteractionFilter<*>, ComponentInteractionRejectionHandler<*>>(context)

        exitProcess(0)
    }

    private inline fun <reified F : Filter, reified H : Any> checkTypes(context: BContext) {
        val filterTypes = context.getInterfacedServiceTypes<F>()
        val rejectionHandlerType = context.getInterfacedServiceTypes<H>().first()

        val filterOutputTypes = filterTypes.map { it.erasureAt<F>(0) }
        check(filterOutputTypes.all { filterOutputTypes.first() == it }) {
            "All ${classRef<F>()} must have the same return type, current types: ${filterOutputTypes.joinToString { it.simpleNestedName }}"
        }

        val rejectionHandlerInputType = rejectionHandlerType.erasureAt<H>(0)
        check(rejectionHandlerInputType.java.isAssignableFrom(filterOutputTypes.first().java)) {
            val inputName = rejectionHandlerInputType.simpleNestedName
            val outputName = filterOutputTypes.first().simpleNestedName
            "Input type of ${classRef<H>()} ($inputName) must be the same (or a superclass) as the output type of ${classRef<F>()} ($outputName)"
        }
    }

    private inline fun <reified T : Any> KClass<*>.erasureAt(index: Int): KClass<*> {
        val interfaceType = allSupertypes.first { it.jvmErasure == T::class }
        return interfaceType.arguments[index].type!!.jvmErasure
    }
}