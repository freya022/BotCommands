package io.github.freya022.botcommands.text

import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.helpers.utils.createTest
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains

// Additional test to disable the help command via configuration
class IHelpCommandAutoConfigurationTest {

    @Test
    fun `Built-in help command can be disabled via configuration`() {
        val context = BotCommands.createTest(textCommands = true) {
            textCommands {
                isHelpDisabled = true
            }
        }

        val exception = assertThrows<ServiceException> {
            context.getService<IHelpCommand>()
        }

        assertContains(exception.message!!, "The help command was disabled in BTextConfig.isHelpDisabled")
    }
}
