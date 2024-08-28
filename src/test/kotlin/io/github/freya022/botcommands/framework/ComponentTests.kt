package io.github.freya022.botcommands.framework

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.test.config.Environment
import io.github.freya022.botcommands.test.config.db.H2DatabaseSource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object ComponentTests {
    private lateinit var context: BContext

    private val buttons: Buttons by lazy { context.getService() }
    private val componentController: ComponentController by lazy { context.getService() }

    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getILoggerFactory() as LoggerContext).loggerList.forEach { it.level = Level.WARN }

        context = BotCommands.create {
            disableExceptionsInDMs = true

            addClass<H2DatabaseSource>()
            addClass<FakeBot>()

            components {
                enable = true
            }

            textCommands {
                enable = false
            }

            applicationCommands {
                enable = false
            }
        }
    }

    @Test
    fun `Group delete results in owned components deleted`(): Unit = runBlocking {
        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(group)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }

    @Test
    fun `Component delete results in owner group deleted`(): Unit = runBlocking {
        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(button)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }
}