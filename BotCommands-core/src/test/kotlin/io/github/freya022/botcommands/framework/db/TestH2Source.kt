package io.github.freya022.botcommands.framework.db

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import java.sql.Connection

@BService
class TestH2Source : ConnectionSupplier {
    private val source = JdbcDataSource().apply {
        setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
    }

    override val maxConnections: Int get() = 1

    override fun getConnection(): Connection = source.connection

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
