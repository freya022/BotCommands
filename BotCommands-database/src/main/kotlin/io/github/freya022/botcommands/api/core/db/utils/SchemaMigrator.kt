package io.github.freya022.botcommands.api.core.db.utils

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
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

    /**
     * Creates a [Flyway] instance configured for this schema.
     * Mainly useful to baseline or repair the schema history.
     */
    fun createFlyway(): Flyway = createFlyway { this }

    /**
     * Creates a [Flyway] instance configured for this schema.
     * Mainly useful to baseline or repair the schema history.
     *
     * @param configure A block to further configure Flyway
     */
    fun createFlyway(configure: FluentConfiguration.() -> FluentConfiguration): Flyway
}
