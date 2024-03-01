package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import kotlinx.coroutines.debug.DebugProbes
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

@InjectedService
interface BDatabaseConfig {
    /**
     * Whether transactions should trigger a coroutine dump & thread dump
     * when running longer than the [max transaction duration][ConnectionSupplier.maxTransactionDuration]
     *
     * **Note:** You need to [install the debug probes][DebugProbes.install] in order to dump coroutine debug info.
     *
     * Default: `false`
     *
     * @see ConnectionSupplier.maxTransactionDuration
     * @see DebugProbes
     * @see DebugProbes.enableCreationStackTraces
     */
    val dumpLongTransactions: Boolean
    /**
     * Determines whether *all* SQL queries should be logged on `TRACE`.
     *
     * The `TRACE` log level is required on the class that created the prepared statement.
     *
     * Default: `false`
     */
    val logQueries: Boolean
    /**
     * Determines if the SQL query logger will replace query parameters by their value.
     *
     * Default: `true`
     */
    val logQueryParameters: Boolean
    /**
     * The duration a query has to run for it to be logged on `WARN`.
     */
    val queryLogThreshold: Duration

    /**
     * The duration a query has to run for it to be logged on `WARN`.
     */
    fun getQueryLogThreshold(): JavaDuration = queryLogThreshold.toJavaDuration()
}

@ConfigDSL
class BDatabaseConfigBuilder internal constructor() : BDatabaseConfig {
    @set:DevConfig
    @set:JvmName("dumpLongTransactions")
    override var dumpLongTransactions: Boolean = false
    @set:JvmName("logQueries")
    override var logQueries: Boolean = false
    @set:JvmName("logQueryParameters")
    override var logQueryParameters: Boolean = true
    @set:JvmSynthetic
    override var queryLogThreshold: Duration = Duration.INFINITE

    /**
     * The duration a query has to run for it to be logged on `WARN`.
     */
    fun setQueryLogThreshold(duration: JavaDuration) {
        this.queryLogThreshold = duration.toKotlinDuration()
    }

    @JvmSynthetic
    internal fun build() = object : BDatabaseConfig {
        override val dumpLongTransactions = this@BDatabaseConfigBuilder.dumpLongTransactions
        override val logQueries = this@BDatabaseConfigBuilder.logQueries
        override val logQueryParameters = this@BDatabaseConfigBuilder.logQueryParameters
        override val queryLogThreshold = this@BDatabaseConfigBuilder.queryLogThreshold
    }
}
