package io.github.freya022.botcommands.helpers.db

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import java.sql.Connection
import javax.sql.DataSource

@BService
open class TestH2Source : ConnectionSupplier {
    protected val source: DataSource = JdbcDataSource().apply {
        setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
    }

    override val maxConnections: Int get() = 1

    override fun getConnection(): Connection = source.connection

    init {
        // Just to make sure old migrations still work
        createFlyway("bc", scriptLocations = arrayOf("bc_database_scripts/generic", "bc_database_scripts/h2")).migrate()
    }

    private fun createFlyway(schema: String, vararg scriptLocations: String): Flyway = Flyway.configure(javaClass.classLoader)
        .dataSource(source)
        .schemas(schema)
        .locations(*scriptLocations)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
}
