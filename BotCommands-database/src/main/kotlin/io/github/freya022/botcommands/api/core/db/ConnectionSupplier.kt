package io.github.freya022.botcommands.api.core.db

import com.zaxxer.hikari.HikariConfig
import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.MissingServiceMessage
import java.sql.Connection
import java.sql.SQLException
import java.time.Duration

/**
 * Enables access to an SQL database.
 *
 * ## Usage
 * Register your instance as a service with [@BService][BService].
 *
 * @see InterfacedService @InterfacedService
 * @see HikariSourceSupplier
 * @see Database
 * @see BlockingDatabase
 */
@InterfacedService(acceptMultiple = false)
@MissingServiceMessage("Using a database required a service implementing ConnectionSupplier")
interface ConnectionSupplier {
    /**
     * The maximum number of connections retrievable without blocking.
     *
     * This is typically the number of connections set up in [HikariConfig.setMaximumPoolSize].
     */
    val maxConnections: Int

    /**
     * Returns the duration until a thread/coroutine dump is attempted during a transaction.
     *
     * This should be the same value as the [HikariCP leak detection threshold][HikariConfig.setLeakDetectionThreshold].
     *
     * @see BDatabaseConfig.dumpLongTransactions
     */
    val maxTransactionDuration: Duration
        get() = Duration.ZERO

    @Throws(SQLException::class)
    fun getConnection(): Connection
}
