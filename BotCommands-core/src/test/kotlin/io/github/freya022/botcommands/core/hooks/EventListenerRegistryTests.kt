package io.github.freya022.botcommands.core.hooks

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirementsProvider
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.hooks.EventListenerRegistry
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent
import net.dv8tion.jda.api.events.emoji.update.GenericEmojiUpdateEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

object EventListenerRegistryTests {
    @Test
    fun `Listeners of Any are forbidden`() {
        class TestListener {
            fun onReady(@Suppress("unused") event: Any) {}
        }

        val ex = assertThrows<IllegalArgumentException> {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService>(),
                customEventRequirementsProviders = emptyList(),
                mockk<FunctionAnnotationsMap> {
                    every { get<BEventListener>() } returns listOf(
                        ClassPathFunction(TestListener(), TestListener::onReady),
                    )
                },
            )
        }

        assertContains(ex.message.orEmpty(), "Function cannot have a first parameter of type: [Object]")
    }

    @Test
    fun `Add event listener`() {
        // In particular, this should test that adding an event listener of sub/super/same-type than an existing listener should work
        //  indicating the dispatch cache is cleared properly

        class A {
            @BEventListener
            fun foo(@Suppress("unused") event: GenericEmojiEvent) {}
        }

        val registry = EventListenerRegistry(
            BConfigBuilder().build(),
            mockk<ServiceContainer>(),
            mockk<JDAService> {
                every { intents } returns setOf(GatewayIntent.GUILD_EXPRESSIONS)
            },
            customEventRequirementsProviders = emptyList(),
            mockk<FunctionAnnotationsMap> {
                every { get<BEventListener>() } returns listOf(
                    ClassPathFunction(A(), A::foo),
                )
            },
        )

        // Generate cache for this event
        assertEquals(1, registry[GenericEmojiUpdateEvent::class.java][RunMode.SHARED]!!.size)

        // Make another listener and make sure the cache grows with the new listeners
        class WithSameType {
            @BEventListener
            fun foo(@Suppress("unused") event: GenericEmojiEvent) {}
        }

        class WithSubType {
            @BEventListener
            fun foo(@Suppress("unused") event: EmojiUpdateNameEvent) {}
        }

        class WithSuperType {
            @BEventListener
            fun foo(@Suppress("unused") event: Event) {}
        }

        registry.addEventListener(WithSameType())
        registry.addEventListener(WithSubType())
        registry.addEventListener(WithSuperType())

        assertEquals(3, registry[GenericEmojiUpdateEvent::class.java][RunMode.SHARED]!!.size)
        assertEquals(4, registry[EmojiUpdateNameEvent::class.java][RunMode.SHARED]!!.size)
    }

    @Test
    fun `Remove event listener`() {
        // Make sure the event listener does not get fired anymore, but similar listeners of same classes (but diff instance) do

        class A {
            @BEventListener
            fun foo(@Suppress("unused") event: Event) {}
        }

        val a1 = A()
        val a2 = A()

        val registry = EventListenerRegistry(
            BConfigBuilder().build(),
            mockk<ServiceContainer>(),
            mockk<JDAService> {
                every { intents } returns setOf()
            },
            customEventRequirementsProviders = emptyList(),
            mockk<FunctionAnnotationsMap> {
                every { get<BEventListener>() } returns listOf(
                    ClassPathFunction(a1, A::foo),
                    ClassPathFunction(a2, A::foo),
                )
            },
        )

        assertEquals(2, registry[Event::class.java][RunMode.SHARED]!!.size)

        registry.removeEventListener(a1)

        assertEquals(a2, registry[Event::class.java][RunMode.SHARED]!!.single().classPathFunction.instance)
    }

    @Test
    fun `JDA Events are dispatched to listeners of all subclasses`() {
        class A {
            @BEventListener
            fun a(@Suppress("unused") event: EmojiUpdateNameEvent) {}
            @BEventListener
            fun b(@Suppress("unused") event: GenericEmojiUpdateEvent<*>) {}
            @BEventListener
            fun c(@Suppress("unused") event: GenericEmojiEvent) {}
            @BEventListener
            fun d(@Suppress("unused") event: Event) {}
        }

        val registry = EventListenerRegistry(
            BConfigBuilder().build(),
            mockk<ServiceContainer>(),
            mockk<JDAService> {
                every { intents } returns setOf(GatewayIntent.GUILD_EXPRESSIONS)
            },
            customEventRequirementsProviders = emptyList(),
            mockk<FunctionAnnotationsMap> {
                val instance = A()
                every { get<BEventListener>() } returns listOf(
                    ClassPathFunction(instance, A::a),
                    ClassPathFunction(instance, A::b),
                    ClassPathFunction(instance, A::c),
                    ClassPathFunction(instance, A::d),
                )
            },
        )

        assertEquals(4, registry[EmojiUpdateNameEvent::class.java][RunMode.SHARED]!!.size)
    }

    @Test
    fun `Custom events are dispatched to listeners of all subclasses`() {
        abstract class MyGenericEvent
        abstract class MyGenericUpdateEvent : MyGenericEvent()
        class MyEvent : MyGenericUpdateEvent()

        class A {
            @BEventListener
            fun a(@Suppress("unused") event: MyEvent) {}
            @BEventListener
            fun b(@Suppress("unused") event: MyGenericUpdateEvent) {}
            @BEventListener
            fun c(@Suppress("unused") event: MyGenericEvent) {}
        }

        val registry = EventListenerRegistry(
            BConfigBuilder().build(),
            mockk<ServiceContainer>(),
            mockk<JDAService> {
                every { intents } returns setOf()
            },
            customEventRequirementsProviders = listOf(object : CustomEventRequirementsProvider {
                override fun get(handledEventType: Class<*>): CustomEventRequirements {
                    if (handledEventType.isSubclassOf<MyGenericEvent>()) {
                        return CustomEventRequirements.none()
                    }
                    return CustomEventRequirements.unknown()
                }
            }),
            mockk<FunctionAnnotationsMap> {
                val instance = A()
                every { get<BEventListener>() } returns listOf(
                    ClassPathFunction(instance, A::a),
                    ClassPathFunction(instance, A::b),
                    ClassPathFunction(instance, A::c),
                )
            },
        )

        assertEquals(3, registry[MyEvent::class.java][RunMode.SHARED]!!.size)
    }

    @Test
    fun `Custom event requirement provider should be checked for implementation correctness`() {
        // Make sure the registry tests all CustomEventRequirementsProvider do not return requirements for other events than theirs

        class CorrectProvider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.unknown()
        }

        class WrongProvider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.none()
        }

        val ex = assertThrows<IllegalStateException> {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService>(),
                customEventRequirementsProviders = listOf(CorrectProvider(), WrongProvider()),
                mockk<FunctionAnnotationsMap>(),
            )
        }

        assertEquals(
            $$"Custom event requirement providers are required to return 'CustomEventRequirements.unknown()' on unhandled events, and 'i.g.f.b.c.h.EventListenerRegistryTests$Custom event requirement provider should be checked for implementation correctness$WrongProvider' fails that",
            ex.message,
        )

        assertDoesNotThrow {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService>(),
                customEventRequirementsProviders = listOf(CorrectProvider()),
                mockk<FunctionAnnotationsMap>(relaxed = true),
            )
        }

        assertDoesNotThrow {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService>(),
                customEventRequirementsProviders = listOf(),
                mockk<FunctionAnnotationsMap>(relaxed = true),
            )
        }
    }

    @Test
    fun `Throw when multiple requirements providers return known requirements`() {
        class MyEvent

        class Provider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements {
                if (handledEventType == MyEvent::class.java) {
                    return CustomEventRequirements.none()
                }
                return CustomEventRequirements.unknown()
            }
        }

        class A {
            @BEventListener
            fun foo(@Suppress("UNUSED_PARAMETER") event: MyEvent) {}
        }

        val ex = assertThrows<IllegalStateException> {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService> {
                    every { intents } returns setOf()
                },
                customEventRequirementsProviders = listOf(Provider(), Provider()),
                mockk<FunctionAnnotationsMap> {
                    every { get<BEventListener>() } returns listOf(
                        ClassPathFunction(A(), A::foo),
                    )
                },
            )
        }

        assertEquals(
            true,
            ex.message?.startsWith($$"Multiple CustomEventRequirementsProvider returned requirements for 'i.g.f.b.c.h.EventListenerRegistryTests$Throw when multiple requirements providers return known requirements$MyEvent'")
        )
    }

    @ParameterizedTest
    @MethodSource("customEventsWithIllegalSubclass")
    fun `Custom events cannot extend JDA and BC events`(instance: Any, function: KFunction<*>, eventType: Class<*>) {
        class Provider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.unknown()
        }

        val ex = assertThrows<IllegalStateException> {
            EventListenerRegistry(
                BConfigBuilder().build(),
                mockk<ServiceContainer>(),
                mockk<JDAService> {
                    every { intents } returns setOf()
                },
                customEventRequirementsProviders = listOf(Provider()),
                mockk<FunctionAnnotationsMap> {
                    every { get<BEventListener>() } returns listOf(
                        ClassPathFunction(instance, function),
                    )
                },
            )
        }

        assertEquals("Custom events must not implement ${eventType.name}!", ex.message)
    }

    @JvmStatic
    fun customEventsWithIllegalSubclass(): List<Arguments> {
        abstract class JdaEvent : GenericEvent
        abstract class BcEvent : BGenericEvent

        class A {
            @BEventListener
            fun jda(@Suppress("UNUSED_PARAMETER") event: JdaEvent) {}

            @BEventListener
            fun bc(@Suppress("UNUSED_PARAMETER") event: BcEvent) {}
        }

        return listOf(
            Arguments.argumentSet("JDA", A(), A::jda, GenericEvent::class.java),
            Arguments.argumentSet("BC", A(), A::bc, BGenericEvent::class.java),
        )
    }
}
