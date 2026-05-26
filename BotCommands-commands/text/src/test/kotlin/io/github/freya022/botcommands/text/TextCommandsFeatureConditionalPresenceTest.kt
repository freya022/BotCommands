package io.github.freya022.botcommands.text

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import kotlin.test.Test

class TextCommandsFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresTextCommands::class)
    }
}