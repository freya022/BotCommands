package io.github.freya022.botcommands.commands.application

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import kotlin.test.Test

class AppCommandsFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresApplicationCommands::class)
    }
}