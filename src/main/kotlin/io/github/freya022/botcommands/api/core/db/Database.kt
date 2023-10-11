package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.api.Logging
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.SQLException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

private val logger = KotlinLogging.logger { }

/**
 * Utility class to use connections given by the [ConnectionSupplier].
 *
 * **Note:** The framework uses its own `bc` schema internally,
 * you will need to create the schema if this is the first time you use the library.
 *
 * **Note 2:** If you use Flyway to manage your database, you can manage the framework's tables with the following snippet:
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
 * In this case, you do not have to create the `bc` schema manually.
 */
@InjectedService("Requires a ConnectionSupplier service")
interface Database {
    val config: BConfig
    val connectionSupplier: ConnectionSupplier

    @Throws(SQLException::class)
    suspend fun fetchConnection(readOnly: Boolean = false): Connection

    //TODO java methods
}

val dbLeakScope = namedDefaultScope("Connection leak watcher", 1)

@OptIn(ExperimentalContracts::class)
@Throws(SQLException::class)
suspend inline fun <R> Database.transactional(readOnly: Boolean = false, block: Transaction.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val connection = fetchConnection(readOnly)

    // The leak test code doesn't use idiomatic Kotlin on purpose
    val maxTransactionDuration = when {
        config.dumpLongTransactions -> connectionSupplier.maxTransactionDuration.toKotlinDuration()
        else -> null
    }
    val logger = maxTransactionDuration?.let { Logging.getLogger() }
    val leakJob: Job? = maxTransactionDuration?.let {
        dbLeakScope.launch {
            delay(maxTransactionDuration)
            logger!!.warn("Transaction took longer than ${maxTransactionDuration.toString(DurationUnit.SECONDS, 2)}:\n${createThreadDump()}")
        }
    }

    try {
        connection.autoCommit = false
        return block(Transaction(this, connection))
    } catch (e: Throwable) {
        connection.rollback()
        throw e
    } finally {
        if (maxTransactionDuration != null) {
            leakJob!!.cancel()

            val duration = measureTime { releaseConnection(connection) }
            if (duration > maxTransactionDuration) {
                logger!!.warn("Commit & close took longer than ${maxTransactionDuration.toString(DurationUnit.SECONDS, 2)}:\n${createThreadDump()}")
            }
        } else {
            releaseConnection(connection)
        }
    }
}

@PublishedApi
internal fun releaseConnection(connection: Connection) {
    // Always commit, if the connection was rolled back, this is a no-op
    // Using a "finally" block is mandatory,
    // as code after the "block" function can be skipped if the user does a non-local return
    connection.commit()
    // HikariCP already resets the properties of the Connection when releasing (closing) it.
    // If a connection pool isn't used, then it'll simply recreate a new Connection anyway.
    connection.close()
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun areDebugProbesInstalled(): Boolean {
    try {
        Class.forName("kotlinx.coroutines.debug.DebugProbes")
    } catch (e: ClassNotFoundException) {
        return false
    }

    return DebugProbes.isInstalled
}

@PublishedApi
@OptIn(ExperimentalCoroutinesApi::class)
internal fun createCoroutineDump(): String? = when {
    areDebugProbesInstalled() -> {
        val outputStream = ByteArrayOutputStream()
        DebugProbes.dumpCoroutines(PrintStream(outputStream))
        outputStream.toByteArray().decodeToString()
    }
    else -> {
        logger.warn("Skipping coroutine dump as debug probes are not installed, use DebugProbes#install from kotlinx-coroutines-debug")
        null
    }
}

@PublishedApi
internal fun createThreadDump(): String = buildString {
    val coroutineDump = createCoroutineDump()
    if (coroutineDump != null) {
        append(coroutineDump)
        appendLine()
        appendLine()
    }

    val bean = ManagementFactory.getThreadMXBean()
    val infos = bean.dumpAllThreads(true, true)
    append(infos.joinToString(""))
}

@Throws(SQLException::class)
@Suppress("MemberVisibilityCanBePrivate")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, block: KPreparedStatement.() -> R): R {
    return fetchConnection(readOnly).use {
        KPreparedStatement(this, it.prepareStatement(sql)).use(block)
    }
}