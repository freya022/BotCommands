package io.github.freya022.botcommands.db

import dev.freya02.botcommands.helpers.AbstractFeatureConditionalPresenceTest
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import kotlin.test.Test

class DatabaseFeatureConditionalPresenceTest : AbstractFeatureConditionalPresenceTest() {
    @Test
    fun `All services are conditional on the feature`() {
        checkForMissingFeatureConditions(RequiresDatabase::class)
    }
}