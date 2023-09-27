package com.freya02.botcommands.api.core.db

import com.freya02.botcommands.api.core.config.BComponentsConfigBuilder
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import java.sql.Connection
import java.sql.SQLException
import java.time.Duration

/**
 * Allows the framework to access a PostgreSQL database.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 * @see BComponentsConfigBuilder.useComponents
 */
@InterfacedService(acceptMultiple = false)
@InjectedService(message = "A service implementing ConnectionSupplier and annotated with @BService needs to be set in order to use the database")
interface ConnectionSupplier {
    val maxConnections: Int

    /**
     * Returns the duration until a thread/coroutine dump is attempted during a transaction.
     *
     * This should be the same value as what HikariCP is using as the leak detection threshold.
     *
     * @see BConfig.dumpLongTransactions
     */
    val maxTransactionDuration: Duration
        get() = Duration.ZERO

    @Throws(SQLException::class)
    fun getConnection(): Connection
}
