package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.test.config.Config
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.context.annotation.Condition as SpringCondition

object TestDatabaseChecker : CustomConditionChecker<TestDatabase>, SpringCondition {
    private val databaseType = TestDatabase.DatabaseType.POSTGRESQL

    override val annotationType: Class<TestDatabase> = TestDatabase::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: TestDatabase): String? {
        val implDatabaseType = annotation.databaseType

        if (databaseType == TestDatabase.DatabaseType.POSTGRESQL && !hasPostgresCredentials()) {
            // Fallback to H2 if that's what we're scanning
            if (implDatabaseType == TestDatabase.DatabaseType.H2) {
                return null
            }

            return "No PostgreSQL database name, or user, or password is set, falling back to H2."
        }

        if (databaseType != implDatabaseType) {
            return "$implDatabaseType is disabled, $databaseType is used instead"
        }

        return null
    }

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val implDatabaseType = metadata.annotations
            .get(TestDatabase::class.java)
            .synthesize().databaseType

        if (databaseType == TestDatabase.DatabaseType.POSTGRESQL && !hasPostgresCredentials()) {
            // Fallback to H2 if that's what we're scanning
            return implDatabaseType == TestDatabase.DatabaseType.H2
        }

        return databaseType == implDatabaseType
    }

    private fun hasPostgresCredentials(): Boolean {
        val databaseConfig = Config.instance.databaseConfig
        return databaseConfig.name.isNotBlank() && databaseConfig.user.isNotBlank() && databaseConfig.password.isNotBlank()
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
