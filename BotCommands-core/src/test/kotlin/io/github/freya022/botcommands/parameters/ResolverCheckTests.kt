package io.github.freya022.botcommands.parameters

import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverFactorySuperclass
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverSuperclass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

object ResolverCheckTests : AbstractIntegrationTest() {
    @Resolver
    annotation class MyResolver

    @MyResolver
    class MetaAnnotatedResolver

    @ResolverFactory
    annotation class MyResolverFactory

    @MyResolverFactory
    class MetaAnnotatedResolverFactory

    @Test
    fun `Resolver with meta-annotation`() {
        assertThrows<MissingResolverSuperclass> {
            createTest {
                addClass<MetaAnnotatedResolver>()
            }
        }
    }

    @Test
    fun `Resolver factory with meta-annotation`() {
        assertThrows<MissingResolverFactorySuperclass> {
            createTest {
                addClass<MetaAnnotatedResolverFactory>()
            }
        }
    }

}
