package io.github.freya022.botcommands.parameters

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.helpers.config.Environment
import io.github.freya022.botcommands.helpers.utils.createTest
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverFactorySuperclass
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverSuperclass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object ResolverCheckTests {
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
            BotCommands.createTest {
                addClass<MetaAnnotatedResolver>()
            }
        }
    }

    @Test
    fun `Resolver factory with meta-annotation`() {
        assertThrows<MissingResolverFactorySuperclass> {
            BotCommands.createTest {
                addClass<MetaAnnotatedResolverFactory>()
            }
        }
    }

}
