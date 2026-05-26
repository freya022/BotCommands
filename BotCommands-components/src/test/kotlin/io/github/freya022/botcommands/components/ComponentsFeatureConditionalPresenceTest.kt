package io.github.freya022.botcommands.components

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import kotlin.test.Test

class ComponentsFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresComponents::class)
    }
}