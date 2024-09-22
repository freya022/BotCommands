package io.github.freya022.botcommands.api.core.db

import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.sql.Connection
import java.sql.SQLException
import java.time.Duration

/**
 * Sub-interface of [ConnectionSupplier] delegating simple [HikariDataSource] getters.
 *
 * @see ConnectionSupplier
 */
@InterfacedService(acceptMultiple = false)
interface HikariSourceSupplier : ConnectionSupplier {
    val source: HikariDataSource

    override val maxConnections: Int
        get() = source.maximumPoolSize
    override val maxTransactionDuration: Duration
        get() = Duration.ofMillis(source.leakDetectionThreshold)

    @Throws(SQLException::class)
    override fun getConnection(): Connection = source.connection
}