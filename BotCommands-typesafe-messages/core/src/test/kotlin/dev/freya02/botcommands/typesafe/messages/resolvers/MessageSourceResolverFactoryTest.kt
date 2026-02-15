package dev.freya02.botcommands.typesafe.messages.resolvers

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.resolvers.MessageSourceResolverFactory
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.ArgumentSet
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.test.assertTrue

object MessageSourceResolverFactoryTest {

    private interface MyMessageSource : IMessageSource
    private interface MyMessageSourceFactory : IMessageSourceFactory<MyMessageSource>

    @MethodSource("handlers")
    @ParameterizedTest
    fun `Resolve message sources in handlers`(function: KFunction<Unit>) {
        val resolverFactory = MessageSourceResolverFactory(
            listOf(mockk<MyMessageSourceFactory>())
        )

        val wrapper = mockk<ParameterWrapper> {
            every { parameter } returns function.valueParameters[1]
            every { erasure } answers { parameter.type.jvmErasure }
        }
        val request = ResolverRequest(wrapper)
        assertTrue(resolverFactory.isResolvable(request))

        assertDoesNotThrow { resolverFactory.get(request) }
    }

    @JvmStatic
    fun handlers(): List<Arguments> = listOf(
        arguments("Message event", ::messageEvent),
        arguments("Command interaction", ::commandInteraction),
        arguments("Component interaction", ::componentInteraction),
    )

    fun arguments(name: String, function: KFunction<Unit>): ArgumentSet {
        return argumentSet(name, function)
    }

    @Suppress("unused")
    private fun messageEvent(event: MessageReceivedEvent, messageSource: MyMessageSource) {}
    @Suppress("unused")
    private fun commandInteraction(interaction: CommandInteraction, messageSource: MyMessageSource) {}
    @Suppress("unused")
    private fun componentInteraction(interaction: ComponentInteraction, messageSource: MyMessageSource) {}
}
