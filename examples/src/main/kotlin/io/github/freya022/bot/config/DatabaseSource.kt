package io.github.freya022.bot.config

import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import java.sql.Connection
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

@BService
@ServiceType(ConnectionSupplier::class)
class DatabaseSource(config: Config) : ConnectionSupplier {
    private val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.databaseConfig.url
        username = config.databaseConfig.user
        password = config.databaseConfig.password

        maximumPoolSize = 2
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        //Migrate BC tables
        createFlyway("bc", "bc_database_scripts").migrate()

        logger.info("Created database source")
    }

    override fun getMaxConnections(): Int = source.maximumPoolSize

    override fun getConnection(): Connection = source.connection

    private fun createFlyway(schema: String, scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(source)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
}