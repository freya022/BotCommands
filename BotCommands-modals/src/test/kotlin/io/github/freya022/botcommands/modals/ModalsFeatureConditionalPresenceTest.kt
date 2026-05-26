package io.github.freya022.botcommands.modals

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import kotlin.test.Test

class ModalsFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresModals::class)
    }
}