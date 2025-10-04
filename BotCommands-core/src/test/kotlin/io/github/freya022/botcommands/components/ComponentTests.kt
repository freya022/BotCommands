package io.github.freya022.botcommands.components

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.helpers.config.Environment
import io.github.freya022.botcommands.helpers.db.TestH2Source
import io.github.freya022.botcommands.helpers.utils.createTest
import io.github.freya022.botcommands.internal.components.controller.ComponentController
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

        context = BotCommands.createTest(components = true) {
            addClass<TestH2Source>()
        }
    }

    @Test
    suspend fun `Group delete results in owned components deleted`() {
        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(group)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }

    @Test
    suspend fun `Component delete results in owner group deleted`() {
        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(button)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }
}
