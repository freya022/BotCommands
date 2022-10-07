package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.SelectionEvent
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.runInitialization
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlin.reflect.full.findAnnotation

@BService
internal class ComponentsHandlerContainer {
    private val buttonMap: MutableMap<String, ComponentDescriptor> = hashMapOf()
    private val selectMap: MutableMap<String, ComponentDescriptor> = hashMapOf()

    @BEventListener
    internal fun onFirstReady(event: FirstReadyEvent, context: BContextImpl, classPathContainer: ClassPathContainer) = runInitialization {
        classPathContainer.functionsWithAnnotation<JDAButtonListener>()
            .requireNonStatic()
            .requireFirstArg(ButtonEvent::class)
            .forEach {
                val handlerName = it.function.findAnnotation<JDAButtonListener>()!!.name

                val oldDescriptor = buttonMap.put(handlerName, ComponentDescriptor(context, it.instance, it.function))
                if (oldDescriptor != null) {
                    throwUser("Tried to override a button handler, old method: ${oldDescriptor.method.shortSignature}, new method: ${it.function.shortSignature}")
                }
            }

        classPathContainer.functionsWithAnnotation<JDASelectionMenuListener>()
            .requireNonStatic()
            .requireFirstArg(SelectionEvent::class)
            .forEach {
                val handlerName = it.function.findAnnotation<JDASelectionMenuListener>()!!.name

                val oldDescriptor = selectMap.put(handlerName, ComponentDescriptor(context, it.instance, it.function))
                if (oldDescriptor != null) {
                    throwUser("Tried to override a select menu handler, old method: ${oldDescriptor.method.shortSignature}, new method: ${it.function.shortSignature}")
                }
            }
    }

    fun getButtonDescriptor(handlerName: String) = buttonMap[handlerName]
    fun getSelectMenuDescriptor(handlerName: String) = selectMap[handlerName]
}