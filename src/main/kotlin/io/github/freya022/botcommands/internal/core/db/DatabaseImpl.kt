package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.db.query.GenericParametrizedQueryFactory
import io.github.freya022.botcommands.internal.core.db.query.NonParametrizedQueryFactory
import io.github.freya022.botcommands.internal.core.db.traced.TracedConnection
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.sql.Connection
import kotlin.time.toKotlinDuration

// If the build script has 3.0.0-alpha.5_DEV, use the next release version, in this case 3.0.0-alpha.6
private const val latestVersion = "3.0.0-alpha.8" // Change in the latest migration script too

private val logger = KotlinLogging.logger { }

@BService
@ServiceType(Database::class)
@Dependencies(ConnectionSupplier::class)
internal class DatabaseImpl internal constructor(
    context: ServiceContainer,
    override val connectionSupplier: ConnectionSupplier,
    override val config: BConfig
) : Database {
    private val tracedQueryFactories: List<ParametrizedQueryFactory<*>> = context.getInterfacedServices()

    private val isQueryThresholdSet = config.queryLogThreshold.isFinite() && config.queryLogThreshold.isPositive()
    private val useTracedConnections = config.logQueries || isQueryThresholdSet

    //Prevents deadlock when a paused coroutine holds a Connection,
    // but cannot be resumed and freed because of the coroutine scope being full (from another component event)
    private val semaphore = Semaphore(connectionSupplier.maxConnections)

    private lateinit var baseSchema: String

    init {
        if (config.dumpLongTransactions) {
            check(connectionSupplier.maxTransactionDuration.toKotlinDuration().isPositive()) {
                "Maximum transaction duration must be positive when ${BConfig::dumpLongTransactions.reference} is enabled"
            }
        }

        runBlocking {
            preparedStatement("select version from bc.bc_version", readOnly = true) {
                val rs = executeQuery().readOrNull()
                    ?: throw IllegalStateException("No version found, please create the BotCommands tables with the migration scripts in 'bc_database_scripts', in the resources folder (you can also use Flyway for example)")

                val version = rs.getString("version")

                if (version != latestVersion) {
                    throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts, don't forget about backups if needed")
                }
            }
        }
    }

    override suspend fun fetchConnection(readOnly: Boolean): Connection {
        semaphore.acquire()
        return connectionSupplier.getConnection()
            .let { connection ->
                if (useTracedConnections) {
                    val tracedQueryFactory = getTracedQueryFactory(connection)
                    TracedConnection(connection, tracedQueryFactory, isQueryThresholdSet, config.queryLogThreshold)
                } else {
                    connection
                }
            }
            .let {
                object : Connection by it {
                    override fun close() {
                        try {
                            it.close()
                        } finally {
                            semaphore.release()
                        }
                    }
                }
            }.also { connection ->
                if (!::baseSchema.isInitialized) {
                    baseSchema = connection.schema
                } else {
                    // Reset schema as it isn't done by HikariCP
                    // in situations where a schema isn't set on the connection pool
                    connection.schema = baseSchema
                }
                connection.isReadOnly = readOnly
            }
    }

    private fun getTracedQueryFactory(connection: Connection): ParametrizedQueryFactory<*> {
        if (!config.logQueryParameters) {
            return NonParametrizedQueryFactory
        }

        val compatibleFactories = tracedQueryFactories.filter { it.isSupported(connection) }
        if (compatibleFactories.isEmpty()) {
            return GenericParametrizedQueryFactory
        } else if (compatibleFactories.size > 1) {
            logger.warn { "Only one ${classRef<ParametrizedQueryFactory<*>>()} should be compatible with $connection, found: ${compatibleFactories.joinToString { it.javaClass.simpleNestedName }}" }
        }

        return compatibleFactories.first()
    }
}