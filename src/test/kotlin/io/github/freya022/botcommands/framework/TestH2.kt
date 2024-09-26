package io.github.freya022.botcommands.framework

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.registerInstanceSupplier
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import org.flywaydb.core.Flyway
import kotlin.time.Duration.Companion.seconds

class TestH2 : HikariSourceSupplier {
    override val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"

        // idk bucket4j is confused without it
        schema = "public"

        maximumPoolSize = 2
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        createFlyway("bc", "bc_database_scripts").migrate()
    }

    private fun createFlyway(schema: String, scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(source)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
}

internal fun BConfigBuilder.addH2(instance: TestH2 = TestH2()) {
    addClass<TestH2>()
    services {
        registerInstanceSupplier { instance }
    }
}