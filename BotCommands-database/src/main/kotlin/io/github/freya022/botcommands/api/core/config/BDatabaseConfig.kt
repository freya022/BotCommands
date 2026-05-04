package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import kotlinx.coroutines.debug.DebugProbes
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

/**
 * Configuration for the database feature.
 *
 * A configuration of this feature must be registered for it to be active.
 * When active, a service implementing [ConnectionSupplier] or [HikariSourceSupplier] must be present.
 *
 * Spring users can set the `botcommands.database.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * [@RequiresDatabase][RequiresDatabase] can be used to disable services when this feature isn't registered.
 *
 * @see [BDatabaseConfig.builder]
 * @see [registerDatabase]
 */
@InjectedService
interface BDatabaseConfig : IConfig, BDatabaseConfigProps {

    override val configType get() = BDatabaseConfig::class.java

    companion object {
        /**
         * Creates a new [BDatabaseConfigBuilder], you must [build][BDatabaseConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BDatabaseConfigBuilder {
            return BDatabaseConfigBuilder.create()
        }
    }
}

interface BDatabaseConfigProps {

    /**
     * Whether transactions should trigger a coroutine dump & thread dump
     * when running longer than the [max transaction duration][ConnectionSupplier.maxTransactionDuration]
     *
     * **Note:** You need to [install the debug probes][DebugProbes.install] in order to dump coroutine debug info.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.database.dumpLongTransactions`
     *
     * @see ConnectionSupplier.maxTransactionDuration
     * @see DebugProbes
     * @see DebugProbes.enableCreationStackTraces
     */
    @get:ConfigurationValue(
        path = "botcommands.database.dumpLongTransactions",
        description = "Whether transactions should trigger a coroutine dump & thread dump when running longer than the max transaction duration, see the documentation for more details.",
        defaultValue = "false",
    )
    val dumpLongTransactions: Boolean

    /**
     * Determines whether *all* SQL queries should be logged on `TRACE`.
     *
     * The `TRACE` log level is required on the class that created the prepared statement.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.database.logQueries`
     */
    @get:ConfigurationValue(
        path = "botcommands.database.logQueries",
        description = "Determines whether *all* SQL queries should be logged on the `TRACE` level of the querying class.",
        defaultValue = "false",
    )
    val logQueries: Boolean

    /**
     * Determines if the SQL query logger will replace query parameters by their value.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.database.logQueryParameters`
     */
    @get:ConfigurationValue(
        path = "botcommands.database.logQueryParameters",
        description = "Determines if the SQL query logger will replace query parameters by their value.",
        defaultValue = "true",
    )
    val logQueryParameters: Boolean

    /**
     * The duration a query has to run for it to be logged on `WARN`.
     *
     * Spring property: `botcommands.database.queryLogThreshold`,
     * see [duration conversions](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations)
     */
    @get:ConfigurationValue(
        path = "botcommands.database.queryLogThreshold",
        description = "The duration a query has to run for it to be logged on `WARN`.",
        type = "java.time.Duration",
    )
    val queryLogThreshold: Duration

    /**
     * The duration a query has to run for it to be logged on `WARN`.
     *
     * Spring property: `botcommands.database.queryLogThreshold`,
     * see [duration conversions](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.durations)
     */
    fun getQueryLogThreshold(): JavaDuration = queryLogThreshold.toJavaDuration()
}

/**
 * Builder of [BDatabaseConfig].
 *
 * @see BDatabaseConfig.builder
 */
@ConfigDSL
class BDatabaseConfigBuilder private constructor() : BDatabaseConfigProps {

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
     *
     * Spring property: `botcommands.database.queryLogThreshold`, **in milliseconds**
     */
    fun setQueryLogThreshold(duration: JavaDuration) {
        this.queryLogThreshold = duration.toKotlinDuration()
    }

    fun build() = object : BDatabaseConfig {
        override val dumpLongTransactions = this@BDatabaseConfigBuilder.dumpLongTransactions
        override val logQueries = this@BDatabaseConfigBuilder.logQueries
        override val logQueryParameters = this@BDatabaseConfigBuilder.logQueryParameters
        override val queryLogThreshold = this@BDatabaseConfigBuilder.queryLogThreshold
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BDatabaseConfigBuilder = BDatabaseConfigBuilder()
    }
}

/**
 * Registers the database feature.
 *
 * @param block A block for further configuration
 *
 * @see BDatabaseConfig
 */
fun BConfigBuilder.registerDatabase(block: BDatabaseConfigBuilder.() -> Unit = { }) {
    val config = BDatabaseConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
