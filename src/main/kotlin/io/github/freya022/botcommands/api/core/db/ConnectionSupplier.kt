package io.github.freya022.botcommands.api.core.db

import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.config.BComponentsConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.sql.Connection
import java.sql.SQLException
import java.time.Duration

/**
 * Allows access to a PostgreSQL database.
 *
 * ## Usage
 * First, register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * ### With Flyway (Recommended)
 * You can run this code after initializing your service:
 * ```kt
 * Flyway.configure()
 *      .dataSource(source)
 *      .schemas("bc")
 *      .locations("bc_database_scripts")
 *      .validateMigrationNaming(true)
 *      .loggers("slf4j")
 *      .load()
 *      .migrate()
 * ```
 * This will run all the migration scripts required to set up your database
 *
 * ### Manual initialization
 * You will have to create a `bc` schema,
 * and run [these scripts](https://github.com/freya022/BotCommands/blob/3.X/src/main/resources/bc_database_scripts)
 * in chronological order.
 *
 * @see InterfacedService @InterfacedService
 * @see BComponentsConfigBuilder.useComponents
 * @see HikariSourceSupplier
 * @see Database
 */
@InterfacedService(acceptMultiple = false)
@InjectedService(message = "A service implementing ConnectionSupplier and annotated with @BService needs to be set in order to use the database")
interface ConnectionSupplier {
    /**
     * The maximum number of connections retrievable without blocking.
     *
     * This is typically the number of connections set up in [HikariDataSource.setMaximumPoolSize].
     */
    val maxConnections: Int

    /**
     * Returns the duration until a thread/coroutine dump is attempted during a transaction.
     *
     * This should be the same value as the [HikariCP leak detection threshold][HikariDataSource.setLeakDetectionThreshold].
     *
     * @see BConfig.dumpLongTransactions
     */
    val maxTransactionDuration: Duration
        get() = Duration.ZERO

    @Throws(SQLException::class)
    fun getConnection(): Connection
}
