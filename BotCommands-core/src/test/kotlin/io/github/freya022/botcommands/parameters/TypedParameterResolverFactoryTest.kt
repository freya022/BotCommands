package io.github.freya022.botcommands.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import kotlin.reflect.full.valueParameters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertTrue

class TypedParameterResolverFactoryTest {

    @Test
    fun `Java types are checked by resolvers`() {
        val request = ResolverRequest(ParameterWrapper(::javaStringListFunc.valueParameters[0]))

        assertTrue(StringListResolverFactory.isResolvable(request))
    }

    private fun javaStringListFunc(@Suppress("unused") list: java.util.List<String>) {}
    private object StringListResolver : IParameterResolver<StringListResolver>
    private object StringListResolverFactory : TypedParameterResolverFactory(typeOf<List<String>>()) {
        override val supportedResolvers: List<Class<out IParameterResolver<*>>> get() = emptyList()
        override fun get(request: ResolverRequest): StringListResolver = StringListResolver
    }
}
