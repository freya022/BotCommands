package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.components.ComponentDescriptor
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberEventFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.full.findAnnotation

@BService
@Dependencies(Components::class)
internal class ComponentsHandlerContainer(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) {
    private val buttonMap: MutableMap<String, ComponentDescriptor> = hashMapOf()
    private val selectMap: MutableMap<String, ComponentDescriptor> = hashMapOf()

    init {
        functionAnnotationsMap.getFunctionsWithAnnotation<JDAButtonListener>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ButtonEvent::class))
            .forEach {
                val handlerName = it.function.findAnnotation<JDAButtonListener>()!!.name

                val oldDescriptor = buttonMap.put(handlerName, ComponentDescriptor(context, it.toMemberEventFunction()))
                if (oldDescriptor != null) {
                    throwUser("Tried to override a button handler, old method: ${oldDescriptor.function.shortSignature}, new method: ${it.function.shortSignature}")
                }
            }

        functionAnnotationsMap.getFunctionsWithAnnotation<JDASelectMenuListener>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(StringSelectEvent::class, EntitySelectEvent::class))
            .forEach {
                val handlerName = it.function.findAnnotation<JDASelectMenuListener>()!!.name

                val oldDescriptor = selectMap.put(handlerName, ComponentDescriptor(context, it.toMemberEventFunction()))
                if (oldDescriptor != null) {
                    throwUser("Tried to override a select menu handler, old method: ${oldDescriptor.function.shortSignature}, new method: ${it.function.shortSignature}")
                }
            }
    }

    fun getButtonDescriptor(handlerName: String) = buttonMap[handlerName]
    fun getSelectMenuDescriptor(handlerName: String) = selectMap[handlerName]
}