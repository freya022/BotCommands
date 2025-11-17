package dev.freya02.botcommands.typesafe.messages.resolvers

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.resolvers.MessageSourceResolverFactory
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.interactions.Interaction
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.test.Test
import kotlin.test.assertTrue

class MessageSourceResolverFactoryTest {

    private interface MyMessageSource : IMessageSource
    private interface MyMessageSourceFactory : IMessageSourceFactory<MyMessageSource>

    @Test
    fun `Resolve message sources from interaction handlers`() {
        val resolverFactory = MessageSourceResolverFactory(
            listOf(object : MyMessageSourceFactory {
                override val bundleName get() = TODO("Not yet implemented")
                override val locales: Set<Locale> get() = TODO()

                override fun create(interaction: Interaction) = TODO("Not yet implemented")
            })
        )

        val wrapper = mockk<ParameterWrapper> {
            every { parameter } returns ::command.valueParameters[1]
            every { erasure } answers { parameter.type.jvmErasure }
        }
        val request = ResolverRequest(wrapper)
        assertTrue(resolverFactory.isResolvable(request))

        assertDoesNotThrow { resolverFactory.get(request) }
    }

    private fun command(event: GlobalSlashEvent, messageSource: MyMessageSource) {}
}
