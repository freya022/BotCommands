package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.EntitySelectEvent
import com.freya02.botcommands.api.components.event.StringSelectEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.core.reflection.FunctionAnnotationsMap
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlin.reflect.full.findAnnotation

@BService
@Dependencies([Components::class])
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