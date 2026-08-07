package io.github.freya022.botcommands.core.hooks

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirements
import io.github.freya022.botcommands.api.core.hooks.custom.CustomEventRequirementsProvider
import io.github.freya022.botcommands.internal.core.hooks.EventListenerRequirementsVerifierImpl
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.events.GenericEvent
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals

object EventListenerRequirementsVerifierTests {

    @Test
    fun `Custom event requirement provider should be checked for implementation correctness`() {
        // Make sure all CustomEventRequirementsProvider do not return requirements for other events than theirs

        class CorrectProvider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.unknown()
        }

        class WrongProvider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.none()
        }

        val ex = assertThrows<IllegalStateException> {
            EventListenerRequirementsVerifierImpl(
                BConfigBuilder().build(),
                mockk<JDAService>(),
                customEventRequirementsProviders = listOf(CorrectProvider(), WrongProvider()),
            )
        }

        assertEquals(
            $$"Custom event requirement providers are required to return 'CustomEventRequirements.unknown()' on unhandled events, and 'i.g.f.b.c.h.EventListenerRequirementsVerifierTests$Custom event requirement provider should be checked for implementation correctness$WrongProvider' fails that",
            ex.message,
        )

        assertDoesNotThrow {
            EventListenerRequirementsVerifierImpl(
                BConfigBuilder().build(),
                mockk<JDAService>(),
                customEventRequirementsProviders = listOf(CorrectProvider()),
            )
        }

        assertDoesNotThrow {
            EventListenerRequirementsVerifierImpl(
                BConfigBuilder().build(),
                mockk<JDAService>(),
                customEventRequirementsProviders = emptyList(),
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
            fun foo(@Suppress("UNUSED_PARAMETER") event: MyEvent) {}
        }

        val ex = assertThrows<IllegalStateException> {
            val verifier = EventListenerRequirementsVerifierImpl(
                BConfigBuilder().build(),
                mockk<JDAService> {
                    every { intents } returns setOf()
                },
                customEventRequirementsProviders = listOf(Provider(), Provider()),
            )

            verifier.verifyFor(A::foo, locallySkipIntentChecks = false, locallySkippedIntents = emptySet(), MyEvent::class.java)
        }

        assertEquals(
            true,
            ex.message?.startsWith($$"Multiple CustomEventRequirementsProvider returned requirements for 'i.g.f.b.c.h.EventListenerRequirementsVerifierTests$Throw when multiple requirements providers return known requirements$MyEvent'")
        )
    }

    @ParameterizedTest
    @MethodSource("customEventsWithIllegalSubclass")
    fun `Custom events cannot extend JDA and BC events`(function: KFunction<*>, eventType: Class<*>, unexpectedEventSubclass: Class<*>) {
        class Provider : CustomEventRequirementsProvider {
            override fun get(handledEventType: Class<*>): CustomEventRequirements = CustomEventRequirements.unknown()
        }

        val ex = assertThrows<IllegalStateException> {
            val verifier = EventListenerRequirementsVerifierImpl(
                BConfigBuilder().build(),
                mockk<JDAService> {
                    every { intents } returns setOf()
                },
                customEventRequirementsProviders = listOf(Provider()),
            )

            verifier.verifyFor(function, locallySkipIntentChecks = false, locallySkippedIntents = emptySet(), eventType)
        }

        assertEquals("Custom events must not implement ${unexpectedEventSubclass.name}!", ex.message)
    }

    @JvmStatic
    fun customEventsWithIllegalSubclass(): List<Arguments> {
        abstract class JdaEvent : GenericEvent
        abstract class BcEvent : BGenericEvent

        class A {
            fun jda(@Suppress("UNUSED_PARAMETER") event: JdaEvent) {}

            fun bc(@Suppress("UNUSED_PARAMETER") event: BcEvent) {}
        }

        return listOf(
            Arguments.argumentSet("JDA", A::jda, JdaEvent::class.java, GenericEvent::class.java),
            Arguments.argumentSet("BC", A::bc, BcEvent::class.java, BGenericEvent::class.java),
        )
    }
}
