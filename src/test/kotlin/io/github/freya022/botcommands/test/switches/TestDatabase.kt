package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.context.annotation.Condition as SpringCondition

object TestDatabaseChecker : CustomConditionChecker<TestDatabase>, SpringCondition {
    private val databaseType = TestDatabase.DatabaseType.H2

    override val annotationType: Class<TestDatabase> = TestDatabase::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: TestDatabase): String? {
        if (databaseType != annotation.databaseType) {
            return "${annotation.databaseType} is disabled, $databaseType is used instead"
        }

        return null
    }

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return databaseType == metadata.annotations
                .get(TestDatabase::class.java)
                .synthesize().databaseType
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestDatabaseChecker::class, fail = false)
@Conditional(TestDatabaseChecker::class)
annotation class TestDatabase(val databaseType: DatabaseType) {
    enum class DatabaseType {
        POSTGRESQL,
        H2
    }
}
