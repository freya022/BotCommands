package io.github.freya022.botcommands.api.core.db.utils

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl.Vendor
import javax.annotation.CheckReturnValue
import javax.sql.DataSource

/**
 * Helper to migrate the legacy schema to per-module schemas.
 *
 * **This does not do the entire migration, refer to the release notes for a tutorial.**
 *
 * ## Requirements
 *
 * You are required to include `org.flywaydb:flyway-core`,
 * PostgreSQL support additionally requires `org.flywaydb:flyway-database-postgresql`.
 *
 * @see of
 */
@Deprecated(message = "This is designed to be a migration helper between 3.X and 4.X, it will be removed in 4.0.0")
object LegacySchemaMigrator {

    /**
     * Creates a new [SchemaMigrator] for the given data source.
     */
    @JvmStatic
    @CheckReturnValue
    fun of(dataSource: DataSource): SchemaMigrator {
        return SchemaMigratorImpl(
            dataSource,
            supportedDatabases = enumSetOf(Vendor.POSTGRESQL, Vendor.H2),
            schemaName = "bc",
            locations = listOf(
                "bc_database_scripts/generic",
                "bc_database_scripts/{vendor_name}",
            ),
        )
    }
}
