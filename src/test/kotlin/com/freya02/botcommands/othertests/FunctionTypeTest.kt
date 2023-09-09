package com.freya02.botcommands.othertests

import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.components.builder.bindTo
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.components.ComponentHandler
import java.util.concurrent.TimeUnit

@JDAButtonListener(name = "test_name")
suspend fun handler(event: ButtonEvent, unit: TimeUnit) {

}

object FunctionTypeTest {
    val x = object : IPersistentActionableComponent {
        override val handler: ComponentHandler?
            get() = null

        override fun bindTo(handlerName: String, vararg data: Any?) {
            println("ok")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val y: suspend (ButtonEvent, TimeUnit) -> String = { x, y -> "" }

        x.bindTo(::handler, TimeUnit.MINUTES)

        for (i in 2..10) {
            val types = (2..i).joinToString { "T$it" }
            val args = (2..i).joinToString { "arg$it" }
            val params = (2..i).joinToString { "arg$it: T$it" }
            println("""
                    inline fun <reified E : GenericComponentInteractionCreateEvent, T1, $types> IPersistentActionableComponent.bindTo(noinline func: suspend (event: E, T1, $types) -> Unit, arg1: T1, $params) {
                        bindToCallable(func as KFunction<*>, E::class, arrayOf(arg1, $args))
                    }
                    """.trimIndent()
            )

            println()
        }

        for (i in 2..10) {
            val types = (2..i).joinToString { "T$it" }
            val args = (2..i).joinToString { "arg$it" }
            val params = (2..i).joinToString { "arg$it: T$it" }
            println("""
                    fun <T1, $types> ModalBuilder.bindToCallable(func: suspend (event: ModalInteractionEvent, T1, $types) -> Unit, arg1: T1, $params) {
                        bindToCallable(func as KFunction<*>, arrayOf(arg1, $args))
                    }
                    """.trimIndent()
            )

            println()
        }
    }
}

