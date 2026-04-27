package io.github.freya022.botcommands.text

import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.config.registerTextCommands
import io.github.freya022.botcommands.api.core.service.tryGetService
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

// Additional test to disable the help command via configuration
class IHelpCommandAutoConfigurationTest : AbstractIntegrationTest() {

    @Test
    fun `Built-in help command can be disabled via configuration`() {
        val context = createTest {
            registerTextCommands {
                isHelpDisabled = true
            }
        }

        val error = assertNotNull(context.tryGetService<IHelpCommand>().serviceError)
        assertContains(error.errorMessage, "The help command was disabled in BTextConfig.isHelpDisabled")
    }
}
