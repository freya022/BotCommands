package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition

object TestDatabaseChecker : CustomConditionChecker<TestDatabase> {
    private val databaseType = TestDatabase.DatabaseType.H2

    override val annotationType: Class<TestDatabase> = TestDatabase::class.java

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: TestDatabase): String? {
        if (databaseType != annotation.databaseType) {
            return "${annotation.databaseType} is disabled, $databaseType is used instead"
        }

        return null
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestDatabaseChecker::class, fail = false)
annotation class TestDatabase(val databaseType: DatabaseType) {
    enum class DatabaseType {
        POSTGRESQL,
        H2
    }
}
