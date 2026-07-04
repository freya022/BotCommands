package io.github.freya022.botcommands.api.commands.application.utils

import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.db.utils.SchemaMigrator
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl.Vendor
import javax.annotation.CheckReturnValue
import javax.sql.DataSource

/**
 * Manages the database tables required by the application commands cache.
 *
 * @see of
 */
object AppCommandsCacheSchemaMigrator {

    /**
     * Creates a new [SchemaMigrator] for the given data source.
     *
     * @see BApplicationConfigBuilder.databaseCache
     */
    @JvmStatic
    @CheckReturnValue
    fun of(dataSource: DataSource): SchemaMigrator {
        return SchemaMigratorImpl(
            dataSource,
            supportedDatabases = enumSetOf(Vendor.POSTGRESQL),
            schemaName = "bc_commands_app",
            locations = listOf(
                "db/bc-migration/app-commands/generic",
                "db/bc-migration/app-commands/{vendor_name}",
            ),
        )
    }
}
