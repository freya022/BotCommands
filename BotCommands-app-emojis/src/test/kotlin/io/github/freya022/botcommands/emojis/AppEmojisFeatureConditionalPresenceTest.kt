package io.github.freya022.botcommands.emojis

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.emojis.annotations.RequiresAppEmojis
import kotlin.test.Test

class AppEmojisFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresAppEmojis::class)
    }
}