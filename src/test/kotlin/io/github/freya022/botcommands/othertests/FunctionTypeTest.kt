package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.PersistentHandlerBuilder
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.ComponentHandler
import java.util.concurrent.TimeUnit

object FunctionTypeTest {
    @JDAButtonListener(name = "test_name")
    fun handler(event: ButtonEvent, unit: TimeUnit) {

    }

    val x = object : IPersistentActionableComponent {
        override val context: BContext
            get() = TODO("Not yet implemented")
        override val handler: ComponentHandler?
            get() = null
        override val filters: MutableList<ComponentInteractionFilter>
            get() = arrayListOf()
        override val rateLimitGroup: String? = null

        override fun rateLimitReference(group: String) { throw NotImplementedError() }

        override fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>) {
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
                        bindToCallable(func as KFunction<*>, E::class, listOf(arg1, $args))
                    }
                    """.trimIndent()
            )

            println()

            println("""
                    inline fun <reified E : GenericComponentInteractionCreateEvent, T1, $types> IPersistentActionableComponent.bindTo(noinline func: (event: E, T1, $types) -> Unit, arg1: T1, $params) {
                        bindToCallable(func as KFunction<*>, E::class, listOf(arg1, $args))
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
                        bindToCallable(func as KFunction<*>, listOf(arg1, $args))
                    }
                    """.trimIndent()
            )

            println()

            println("""
                    fun <T1, $types> ModalBuilder.bindToCallable(func: (event: ModalInteractionEvent, T1, $types) -> Unit, arg1: T1, $params) {
                        bindToCallable(func as KFunction<*>, listOf(arg1, $args))
                    }
                    """.trimIndent()
            )

            println()
        }
    }
}

