@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package io.github.freya022.botcommands.othertests

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.PersistentHandlerBuilder
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import java.util.concurrent.TimeUnit

object FunctionTypeTest {
    @JDAButtonListener(name = "test_name")
    fun handler(event: ButtonEvent, unit: TimeUnit) {

    }

    private object FakeActionableComponent : IPersistentActionableComponent<FakeActionableComponent> {
        override val context: BContext
            get() = throw UnsupportedOperationException()
        override val filters: MutableList<ComponentInteractionFilter<*>>
            get() = arrayListOf()

        override fun rateLimitReference(reference: ComponentRateLimitReference): FakeActionableComponent =
            throw UnsupportedOperationException()

        override fun addFilter(filter: ComponentInteractionFilter<*>): FakeActionableComponent =
            throw UnsupportedOperationException()

        override fun addFilter(filterType: Class<out ComponentInteractionFilter<*>>): FakeActionableComponent =
            throw UnsupportedOperationException()

        override fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>): FakeActionableComponent {
            println("ok")
            return this
        }
    }

    private val x = FakeActionableComponent

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

