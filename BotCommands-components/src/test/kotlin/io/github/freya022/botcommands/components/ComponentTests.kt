package io.github.freya022.botcommands.components

import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.config.registerComponents
import io.github.freya022.botcommands.api.core.config.registerDatabase
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

object ComponentTests : AbstractIntegrationTest() {
    private val buttons: Buttons
        get() = context.getService()
    private val componentController: ComponentController
        get() = context.getService()

    @Test
    suspend fun `Group delete results in owned components deleted`() {
        createTest {
            addClass<ComponentsTestH2Source>()

            registerDatabase()
            registerComponents()
        }

        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(group)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }

    @Test
    suspend fun `Component delete results in owner group deleted`() {
        createTest {
            addClass<ComponentsTestH2Source>()

            registerDatabase()
            registerComponents()
        }

        val button = buttons.primary("test").persistent { }
        val group = buttons.group(button).ephemeral { }

        buttons.deleteComponents(button)

        assertNull(componentController.getActiveComponent(group.internalId))
        assertNull(componentController.getActiveComponent(button.internalId))
    }
}
