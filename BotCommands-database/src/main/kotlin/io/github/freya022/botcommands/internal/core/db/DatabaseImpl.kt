package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.db.query.GenericParametrizedQueryFactory
import io.github.freya022.botcommands.internal.core.db.query.NonParametrizedQueryFactory
import io.github.freya022.botcommands.internal.core.db.traced.TracedConnection
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Semaphore
import java.sql.Connection
import kotlin.time.toKotlinDuration

private val logger = KotlinLogging.loggerOf<Database>()

@BService
@Primary
@RequiresDatabase
internal class DatabaseImpl internal constructor(
    override val connectionSupplier: ConnectionSupplier,
    override val databaseConfig: BDatabaseConfig,
    private val tracedQueryFactories: List<ParametrizedQueryFactory<*>>
) : Database {
    internal open class ConnectionResource internal constructor(protected val connection: Connection, private val semaphore: Semaphore) : Connection by connection {
        internal val availablePermits get() = semaphore.availablePermits

        override fun close() {
            try {
                connection.close()
            } finally {
                semaphore.release()
            }
        }

        override fun isWrapperFor(iface: Class<*>): Boolean =
            iface.isInstance(this) || connection.isWrapperFor(iface)

        override fun <T : Any> unwrap(iface: Class<T>): T = when {
            iface.isInstance(this) -> iface.cast(this)
            else -> connection.unwrap(iface)
        }
    }

    private val isQueryThresholdSet = databaseConfig.queryLogThreshold.isFinite() && databaseConfig.queryLogThreshold.isPositive()
    private val useTracedConnections = databaseConfig.logQueries || isQueryThresholdSet

    //Prevents deadlock when a paused coroutine holds a Connection,
    // but cannot be resumed and freed because of the coroutine scope being full (from another component event)
    private val semaphore = Semaphore(connectionSupplier.maxConnections)

    init {
        if (databaseConfig.dumpLongTransactions) {
            check(connectionSupplier.maxTransactionDuration.toKotlinDuration().isPositive()) {
                "Maximum transaction duration must be positive when ${BDatabaseConfig::dumpLongTransactions.reference} is enabled"
            }
        }
    }

    override suspend fun fetchConnection(readOnly: Boolean): Connection {
        semaphore.acquire()
        val rawConnection = try {
            connectionSupplier.getConnection()
        } catch (e: Exception) {
            semaphore.release()
            throw e
        }

        val connection = try {
            if (useTracedConnections) {
                val tracedQueryFactory = getTracedQueryFactory(rawConnection)
                TracedConnection(rawConnection, semaphore, tracedQueryFactory, databaseConfig.logQueries, isQueryThresholdSet, databaseConfig.queryLogThreshold)
            } else {
                ConnectionResource(rawConnection, semaphore)
            }
        } catch (e: Exception) {
            semaphore.release()
            runCatching { rawConnection.close() }.onFailure { e.addSuppressed(it) }
            throw e
        }

        try {
            connection.isReadOnly = readOnly
        } catch (e: Exception) {
            runCatching { connection.close() }.onFailure { e.addSuppressed(it) }
            throw e
        }

        return connection
    }

    private lateinit var parametrizedQueryFactory: ParametrizedQueryFactory<*>

    private fun getTracedQueryFactory(connection: Connection): ParametrizedQueryFactory<*> {
        if (::parametrizedQueryFactory.isInitialized) {
            return parametrizedQueryFactory
        }

        return synchronized(this) {
            if (::parametrizedQueryFactory.isInitialized) {
                return parametrizedQueryFactory
            }

            parametrizedQueryFactory = createTracedQueryFactory(connection)
            parametrizedQueryFactory
        }
    }

    private fun createTracedQueryFactory(connection: Connection): ParametrizedQueryFactory<*> {
        if (!databaseConfig.logQueryParameters) {
            return NonParametrizedQueryFactory
        }

        val metaData = connection.metaData
        val compatibleFactories = tracedQueryFactories.filter { it.isSupported(connection, metaData) }
        if (compatibleFactories.isEmpty()) {
            return GenericParametrizedQueryFactory
        } else if (compatibleFactories.size > 1) {
            logger.warn { "Only one ${classRef<ParametrizedQueryFactory<*>>()} should be compatible with $connection, found: ${compatibleFactories.joinToString { it.javaClass.simpleNestedName }}" }
        }

        return compatibleFactories.first()
    }
}
