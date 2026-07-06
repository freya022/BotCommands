package io.github.freya022.botcommands.internal.core.db.utils

import io.github.freya022.botcommands.api.core.db.utils.SchemaMigrator
import io.github.freya022.botcommands.api.core.utils.mapToArray
import io.github.freya022.botcommands.internal.utils.throwState
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

class SchemaMigratorImpl(
    private val dataSource: DataSource,
    private val supportedDatabases: Set<Vendor>,
    private val schemaName: String,
    private val locations: List<String>,
) : SchemaMigrator {

    private var classLoader: ClassLoader? = null

    init {
        checkModuleClassIsPresent(className = "org.flywaydb.core.Flyway", moduleName = "org.flywaydb:flyway-core")
    }

    override fun setClassLoader(classLoader: ClassLoader): SchemaMigrator = apply {
        this.classLoader = classLoader
    }

    override fun migrate() {
        createFlyway().migrate()
    }

    override fun createFlyway(configure: FluentConfiguration.() -> FluentConfiguration): Flyway {
        val dbName = dataSource.connection.use { connection -> connection.metaData.databaseProductName }
        val vendor = when {
            dbName.startsWith("PostgreSQL") -> Vendor.POSTGRESQL
            dbName == "H2" -> Vendor.H2
            else -> throw UnsupportedOperationException("'$dbName' is not supported")
        }

        require(vendor in supportedDatabases) {
            "${vendor.databaseName} is not supported by this schema"
        }

        vendor.checkModuleIsPresent()

        return Flyway.configure(classLoader ?: javaClass.classLoader)
            .dataSource(dataSource)
            .schemas(schemaName)
            .locations(*locations.mapToArray { it.replace("{vendor_name}", vendor.folderName) })
            .validateMigrationNaming(true)
            .failOnMissingLocations(true)
            .loggers("slf4j")
            .let(configure)
            .load()
    }

    enum class Vendor(val databaseName: String, val folderName: String) {
        POSTGRESQL("PostgreSQL", "postgresql") {

            override fun checkModuleIsPresent() {
                checkModuleClassIsPresent(
                    className = "org.flywaydb.database.postgresql.PostgreSQLDatabaseType",
                    moduleName = "org.flywaydb:flyway-database-postgresql",
                )
            }
        },
        H2("H2", "h2") {

            override fun checkModuleIsPresent() {
                // Core is sufficient
            }
        },
        ;

        abstract fun checkModuleIsPresent()
    }

    private companion object {

        private fun checkModuleClassIsPresent(className: String, moduleName: String) {
            try {
                Class.forName(className, false, Thread.currentThread().contextClassLoader)
            } catch (_: ClassNotFoundException) {
                throwState("The '$moduleName' module is missing")
            }
        }
    }
}
