package io.github.freya022.botcommands.framework

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.resolvers.UserResolver
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.entities.User
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertIsNot

class ParameterResolverFactoryTest {

    @Test
    fun `Resolver factories can be overridden by priority`() {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf("userResolver")
            every { findAnnotationOnService("userResolver", Resolver::class) } returns Resolver(0)
            every { getService(BotCommandsMessagesFactory::class) } returns mockk()
        }
        val context = mockk<BContext> {
            every { this@mockk.serviceContainer } returns serviceContainer
        }
        every { serviceContainer.getService("userResolver", ParameterResolver::class) } returns UserResolver(context)
        val resolvers = ResolverContainer(serviceContainer, listOf(OverrideableUserResolverFactory))

        val request = ResolverRequest(ParameterWrapper(::userFunc.valueParameters[0]))

        // Test our resolver is overridden by built-in
        run {
            resolvers.clearCache()
            OverrideableUserResolverFactory.priority = -1
            val resolver = resolvers.getResolver(IParameterResolver::class, request)
            assertIsNot<OverrideableUserResolver>(resolver)
        }

        // Test opposite is also true
        run {
            resolvers.clearCache()
            OverrideableUserResolverFactory.priority = 1
            val resolver = resolvers.getResolver(IParameterResolver::class, request)
            assertIs<OverrideableUserResolver>(resolver)
        }
    }

    private fun userFunc(@Suppress("unused") user: User) {}
    private object OverrideableUserResolver : ClassParameterResolver<OverrideableUserResolver, User>(User::class), ICustomResolver<OverrideableUserResolver, User>
    private object OverrideableUserResolverFactory : TypedParameterResolverFactory<OverrideableUserResolver>(OverrideableUserResolver::class, User::class) {
        override var priority: Int = 0
        override fun get(request: ResolverRequest): OverrideableUserResolver = OverrideableUserResolver
    }
}
