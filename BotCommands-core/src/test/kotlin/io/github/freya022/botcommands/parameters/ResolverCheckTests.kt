package io.github.freya022.botcommands.parameters

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.helpers.config.Environment
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverFactorySuperclass
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverSuperclass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object ResolverCheckTests : AbstractIntegrationTest() {
    @Resolver
    annotation class MyResolver

    @MyResolver
    class MetaAnnotatedResolver

    @ResolverFactory
    annotation class MyResolverFactory

    @MyResolverFactory
    class MetaAnnotatedResolverFactory

    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getILoggerFactory() as LoggerContext).loggerList.forEach { it.level = Level.WARN }
    }

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
