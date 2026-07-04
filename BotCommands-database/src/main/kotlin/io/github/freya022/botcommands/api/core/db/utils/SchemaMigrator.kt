package io.github.freya022.botcommands.api.core.db.utils

import javax.annotation.CheckReturnValue

/**
 * Utility interface to migrate relational database schemas.
 *
 * Similarly-named classes will be available for modules which have their own schema.
 */
interface SchemaMigrator {

    /**
     * Sets the class loader to use when loading the migration scripts.
     */
    @CheckReturnValue
    fun setClassLoader(classLoader: ClassLoader): SchemaMigrator

    /**
     * Runs the migration.
     *
     * This requires the `org.flywaydb:flyway-core` module,
     * PostgreSQL support additionally requires `org.flywaydb:flyway-database-postgresql`.
     *
     * @throws IllegalStateException If the required Flyway dependencies are not present
     */
    fun migrate()
}
