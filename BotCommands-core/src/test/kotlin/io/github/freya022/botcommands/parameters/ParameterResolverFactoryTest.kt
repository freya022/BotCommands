package io.github.freya022.botcommands.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.*
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.TypedResolverRequest
import io.mockk.every
import io.mockk.mockk
import net.dv8tion.jda.api.entities.User
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ParameterResolverFactoryTest {

    @Test
    fun `Supported resolvers are checked`() {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf()
        }

        val incorrect = object : ParameterResolverFactory() {
            override val supportedTypesStr: List<String> = emptyList()
            override val supportedResolvers: List<Class<out IParameterResolver<*>>> = listOf(IParameterResolver::class.java)
            override fun isResolvable(request: ResolverRequest): Boolean = false
            override fun get(request: ResolverRequest): IParameterResolver<*> = throw UnsupportedOperationException()
        }

        assertThrows<IllegalArgumentException> { ResolverContainer(serviceContainer, listOf(incorrect), listOf()) }
            .also { e ->
                assertTrue("but it is not a built-in resolver" in e.message!!)
            }

        val correct = object : ParameterResolverFactory() {
            override val supportedTypesStr: List<String> = emptyList()
            override val supportedResolvers: List<Class<out IParameterResolver<*>>> = listOf(ICustomResolver::class.java)
            override fun isResolvable(request: ResolverRequest): Boolean = false
            override fun get(request: ResolverRequest): IParameterResolver<*> = throw UnsupportedOperationException()
        }

        assertDoesNotThrow { ResolverContainer(serviceContainer, listOf(correct), listOf()) }
    }

    @Test
    fun `Resolver factories can be overridden by priority`() {
        val serviceContainer = mockk<ServiceContainer> {
            every { getServiceNamesForAnnotation(Resolver::class) } returns listOf("userResolver")
            every { findAnnotationOnService("userResolver", Resolver::class) } returns Resolver(0)
            every { getService("userResolver", ParameterResolver::class) } returns CustomUserResolver
        }
        val resolvers = ResolverContainer(serviceContainer, listOf(OverrideableUserResolverFactory), listOf())

        val request = TypedResolverRequest(ICustomResolver::class.java, ParameterWrapper(::userFunc.valueParameters[0]))

        // Test our resolver is overridden by built-in
        run {
            resolvers.clearCache()
            OverrideableUserResolverFactory.priority = -1
            val resolver = resolvers.getResolver(request)
            assertIs<CustomUserResolver>(resolver)
        }

        // Test opposite is also true
        run {
            resolvers.clearCache()
            OverrideableUserResolverFactory.priority = 1
            val resolver = resolvers.getResolver(request)
            assertIs<OverrideableUserResolver>(resolver)
        }
    }

    private fun userFunc(@Suppress("unused") user: User) {}
    private object CustomUserResolver : ClassParameterResolver<OverrideableUserResolver, User>(User::class), ICustomResolver<OverrideableUserResolver, User>
    private object OverrideableUserResolver : ClassParameterResolver<OverrideableUserResolver, User>(User::class), ICustomResolver<OverrideableUserResolver, User>
    private object OverrideableUserResolverFactory : TypedParameterResolverFactory(User::class) {
        override var priority: Int = 0
        override val supportedResolvers: List<Class<out IParameterResolver<*>>> = listOf(ICustomResolver::class.java)
        override fun get(request: ResolverRequest): OverrideableUserResolver = OverrideableUserResolver
    }
}
