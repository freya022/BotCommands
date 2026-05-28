package dev.freya02.botcommands.bot.config.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.freya02.botcommands.bot.config.Config
import dev.freya02.botcommands.bot.switches.TestDatabase
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.Flyway
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

// Interfaced service used to retrieve an SQL Connection
@BService
@TestDatabase(TestDatabase.DatabaseType.POSTGRESQL)
class PostgresDatabaseSource(config: Config) : HikariSourceSupplier {
    override val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.databaseConfig.url
        username = config.databaseConfig.user
        password = config.databaseConfig.password

        maximumPoolSize = 2
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        //Migrate BC tables
        createFlyway("bc_commands_app", scriptLocations = arrayOf("db/bc-migration/app-commands/generic", "db/bc-migration/app-commands/postgresql")).migrate()
        createFlyway("bc_components", scriptLocations = arrayOf("db/bc-migration/components/generic", "db/bc-migration/components/postgresql")).migrate()

        //You can use the same function for your database, you just have to change the schema and scripts location
        //Migrate BC test tables
        createFlyway("public", "bc_test_database_scripts").migrate()

        logger.info { "Created database source" }
    }

    private fun createFlyway(schema: String, vararg scriptLocations: String): Flyway = Flyway.configure(javaClass.classLoader)
        .dataSource(source)
        .schemas(schema)
        .locations(*scriptLocations)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
}
