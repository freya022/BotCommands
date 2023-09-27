package com.freya02.botcommands.api.core.db

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.time.Duration

/**
 * Sub-interface of [ConnectionSupplier] delegating simple [HikariDataSource] getters.
 *
 * @see ConnectionSupplier
 */
interface HikariSourceSupplier : ConnectionSupplier {
    val source: HikariDataSource

    override val maxConnections: Int
        get() = source.maximumPoolSize
    override val maxTransactionDuration: Duration
        get() = Duration.ofMillis(source.leakDetectionThreshold)

    @Throws(SQLException::class)
    override fun getConnection(): Connection = source.connection
}