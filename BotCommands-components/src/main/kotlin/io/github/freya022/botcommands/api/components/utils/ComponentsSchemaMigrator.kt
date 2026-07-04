package io.github.freya022.botcommands.api.components.utils

import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.db.utils.SchemaMigrator
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl
import io.github.freya022.botcommands.internal.core.db.utils.SchemaMigratorImpl.Vendor
import javax.annotation.CheckReturnValue
import javax.sql.DataSource

/**
 * Manages the database tables required by the components module.
 *
 * @see of
 */
object ComponentsSchemaMigrator {

    /**
     * Creates a new [SchemaMigrator] for the given data source.
     *
     * @see BComponentsConfig
     */
    @JvmStatic
    @CheckReturnValue
    fun of(dataSource: DataSource): SchemaMigrator {
        return SchemaMigratorImpl(
            dataSource,
            supportedDatabases = enumSetOf(Vendor.POSTGRESQL, Vendor.H2),
            schemaName = "bc_components",
            locations = listOf(
                "db/bc-migration/components/generic",
                "db/bc-migration/components/{vendor_name}",
            ),
        )
    }
}
