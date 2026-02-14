package io.github.freya022.botcommands.components

import dev.freya02.botcommands.helpers.AbstractIntegrationTest
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.config.registerLocalComponents
import io.github.freya022.botcommands.api.core.service.getService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

object LocalComponentsTests : AbstractIntegrationTest() {

    @Test
    fun `Create ephemeral component`() {
        val context = createTest {
            registerLocalComponents()
        }

        val buttons = context.getService<Buttons>()
        runBlocking {
            buttons.primary("Test").ephemeral {
                bindTo { println("Clicked") }
                timeout(10.seconds) { println("Timeout") }
            }
        }
    }
}
