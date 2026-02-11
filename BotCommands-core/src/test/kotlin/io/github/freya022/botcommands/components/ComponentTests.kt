package io.github.freya022.botcommands.components

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.helpers.config.Environment
import io.github.freya022.botcommands.helpers.db.TestH2Source
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object ComponentTests : AbstractIntegrationTest() {
    private val buttons: Buttons
        get() = context.getService()
    private val componentController: ComponentController
        get() = context.getService()

    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getILoggerFactory() as LoggerContext).loggerList.forEach { it.level = Level.WARN }

        createTest(components = true) {
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
