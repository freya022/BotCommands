@file:IgnoreStackFrame // Due to TracedConnection
@file:OptIn(ExperimentalContracts::class)

package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.api.core.Logging.toUnwrappedLogger
import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.internal.core.db.DatabaseImpl
import io.github.freya022.botcommands.internal.utils.StackSensitive
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.findCaller
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import org.intellij.lang.annotations.Language
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

private val logger = KotlinLogging.logger { }

/**
 * Utility class to use connections given by the [ConnectionSupplier], in a suspending style.
 *
 * Use [BlockingDatabase] if you don't use Kotlin.
 *
 * ### Nested transaction
 * Nested transactions are transactions that use an existing connection,
 * enabling you to, for example, have a [transactional] inside another.
 *
 * They are supported on methods that use callbacks and work with coroutines.
 *
 * #### Nested transaction block
 * Queries executed in a nested transaction are not committed when exiting the block,
 * only top-level transactions do.
 *
 * #### Read-only status
 * A nested transaction cannot be created
 * if the existing connection is read-only and the nested transaction is read-write.
 *
 * - ✓ Read-write -> Read-only
 * - ✓ Read-only -> Read-only
 * - ✗ Read-only -> Read-write
 *
 * Keep in mind that a read-only transaction will not prevent write operation,
 * as they mostly [enable optimizations][Connection.setReadOnly],
 * and only a few databases reject modifying queries.
 *
 * ### Tracing
 * The connection could be wrapped depending on the configuration, for example,
 * to log the queries (in which case a [ParametrizedQuery] is used), as well as timing them.
 *
 * A SQL statement is traced if any of these conditions is met:
 * - [BDatabaseConfig.logQueries] is enabled,
 *   and the logger of the class that created the prepared statement has its `TRACE` logs enabled,
 * - [BDatabaseConfig.queryLogThreshold] is configured
 *
 * The logged SQL statements will use the logger of the class that created the prepared statement.
 * If a utility class creates statements, you should use [@IgnoreStackFrame][IgnoreStackFrame],
 * which will instead take the logger of the class that called your utility class.
 * You can also use [PreparedStatement.withLogger] if you wish to use a different logger.
 *
 * ### Batching support
 *
 * If you must run a lot of DML statements (`INSERT`, `UPDATE`, ...),
 * you can batch them as to execute all of them in one go, massively improving performances on larger updates.
 *
 * For that, you can use any function giving you a [prepared statement][SuspendingPreparedStatement], then,
 * you can add statements by:
 * - Adding the parameters using [SuspendingPreparedStatement.setParameters],
 * - Calling [SuspendingPreparedStatement.addBatch].
 *
 * Repeat those two steps for all your statements,
 * then call [SuspendingPreparedStatement.executeBatch] to run all of them.
 *
 * **Note:** To read returned columns (like an `INSERT INTO ... RETURNING {column}` in PostgreSQL),
 * you must specify the column indexes/names when creating your statement,
 * and read them back from [SuspendingPreparedStatement.getGeneratedKeys].
 *
 * @see RequiresDatabase @RequiresDatabase
 * @see BlockingDatabase
 * @see ParametrizedQueryFactory
 */
@InterfacedService(acceptMultiple = false)
interface Database {
    val databaseConfig: BDatabaseConfig
    val connectionSupplier: ConnectionSupplier

    /**
     * Acquires a database connection.
     *
     * If [all connections are used][ConnectionSupplier.maxConnections],
     * this function suspends until a connection is available.
     *
     * The connection should always be short-lived, consider using [transactional] otherwise.
     *
     * The returned connection **must** be closed with an [use] closure.
     *
     * @param readOnly `true` if the database only is read from, can allow some optimizations
     *                 but does **not** prevent writing
     *
     * @see transactional
     * @see preparedStatement
     */
    @JvmSynthetic
    @Throws(SQLException::class)
    suspend fun fetchConnection(readOnly: Boolean = false): Connection
}

@JvmOverloads
@Throws(SQLException::class)
internal fun Database.fetchConnectionJava(readOnly: Boolean = false): Connection =
    runBlocking { fetchConnection(readOnly) }

@JvmOverloads
@Throws(SQLException::class)
internal fun <R> Database.withTransactionJava(readOnly: Boolean = false, block: TransactionFunction<R, *>): R =
    runBlocking { transactional(readOnly) { block.apply(BlockingTransaction(connection)) } }

@Suppress("SqlSourceToSinkFlow")
@JvmOverloads
@Throws(SQLException::class)
internal fun <R> Database.withStatementJava(sql: String, readOnly: Boolean = false, block: StatementFunction<R, *>): R =
    runBlocking {
        withReusedConnection(readOnly) { connection ->
            BlockingPreparedStatement(connection.prepareStatement(sql)).use { block.apply(it) }
        }
    }

@Suppress("SqlSourceToSinkFlow")
@JvmOverloads
@Throws(SQLException::class)
internal fun <R> Database.withStatementJava(sql: String, readOnly: Boolean = false, columnIndexes: IntArray, block: StatementFunction<R, *>): R =
    runBlocking {
        withReusedConnection(readOnly) { connection ->
            BlockingPreparedStatement(connection.prepareStatement(sql, columnIndexes)).use { block.apply(it) }
        }
    }

@Suppress("SqlSourceToSinkFlow")
@JvmOverloads
@Throws(SQLException::class)
internal fun <R> Database.withStatementJava(sql: String, readOnly: Boolean = false, columnNames: Array<out String>, block: StatementFunction<R, *>): R =
    runBlocking {
        withReusedConnection(readOnly) { connection ->
            BlockingPreparedStatement(connection.prepareStatement(sql, columnNames)).use { block.apply(it) }
        }
    }

@PublishedApi
internal val dbLeakScope = namedDefaultScope("Connection leak watcher", 1)

private val currentTransaction = ThreadLocal<Transaction>()

/**
 * Acquires a database connection, runs the [block], commits the changes and closes the connection.
 *
 * If [all connections are used][ConnectionSupplier.maxConnections],
 * this function suspends until a connection is available.
 *
 * If [BDatabaseConfig.dumpLongTransactions] is enabled,
 * a coroutine dump ([if available][BDatabaseConfig.dumpLongTransactions]) and a thread dump will be done
 * if the transaction is longer than [the threshold][ConnectionSupplier.maxTransactionDuration].
 *
 * Supports nesting, but it is not recommended doing so,
 * avoid nesting by returning the data as soon as possible.
 *
 * @param readOnly `true` if the database only is read from, can allow some optimizations
 *                 but does **not** prevent writing
 *
 * @see Database.fetchConnection
 * @see Database.preparedStatement
 *
 * @see BDatabaseConfig.dumpLongTransactions
 * @see ConnectionSupplier.maxTransactionDuration
 */
@Throws(SQLException::class)
suspend inline fun <R> Database.transactional(readOnly: Boolean = false, crossinline block: suspend Transaction.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    currentTransactionIfExists(readOnly)?.let { return block(it) }

    return withNewTransaction(readOnly) { connection, transaction ->
        measureTransactionDurationAndRelease(connection) {
            try {
                connection.autoCommit = false
                block(transaction)
            } catch (e: Throwable) {
                connection.rollback()
                throw e
            }
        }
    }
}

@PublishedApi
internal fun currentTransactionIfExists(readOnly: Boolean) =
    currentTransaction.get()?.also {
        val isCurrentlyReadOnly = it.connection.isReadOnly
        // Allow if connection is RW, or R-O is requested
        check(!isCurrentlyReadOnly || readOnly) {
            "Cannot reuse a read-only transaction as a read-write transaction"
        }
    }

@PublishedApi
internal suspend fun <R> Database.withNewTransaction(
    readOnly: Boolean,
    block: suspend (connection: Connection, transaction: Transaction) -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val connection = fetchConnection(readOnly)
    val transaction = Transaction(connection)
    return withContext(currentTransaction.asContextElement(value = transaction)) {
        block(connection, transaction)
    }
}

@OptIn(StackSensitive::class)
@PublishedApi
internal suspend fun <R> Database.measureTransactionDurationAndRelease(connection: Connection, block: suspend () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    if (!databaseConfig.dumpLongTransactions) {
        try {
            return block()
        } finally {
            releaseConnection(connection)
        }
    }

    val callerFrame = findCaller(skip = 1)
    fun getLogger() = callerFrame.declaringClass.toUnwrappedLogger()

    val maxTransactionDuration = connectionSupplier.maxTransactionDuration.toKotlinDuration()
    val leakJob = dbLeakScope.launch {
        delay(maxTransactionDuration)
        getLogger().warn { formatExceededTransactionDuration("Transaction", connection, maxTransactionDuration) }
    }

    try {
        return block()
    } finally {
        leakJob.cancel()

        val duration = measureTime { releaseConnection(connection) }
        if (duration > maxTransactionDuration) {
            getLogger().warn { formatExceededTransactionDuration("Commit & close", connection, maxTransactionDuration) }
        }
    }
}

private fun formatExceededTransactionDuration(actionDescription: String, connection: Connection, maxTransactionDuration: Duration): String {
    if (!connection.isWrapperFor(DatabaseImpl.ConnectionResource::class.java))
        throwInternal("Transaction connection should have been a ${classRef<DatabaseImpl.ConnectionResource>()}")
    val resource = connection.unwrap(DatabaseImpl.ConnectionResource::class.java)
    val availablePermits = resource.availablePermits

    return "$actionDescription took longer than ${maxTransactionDuration.toString(DurationUnit.SECONDS, 2)} (available permits: $availablePermits):\n${createDumps()}"
}

private fun releaseConnection(connection: Connection) {
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
        logger.warn { "Skipping coroutine dump as debug probes are not installed, use DebugProbes#install from kotlinx-coroutines-debug" }
        null
    }
}

@PublishedApi
internal fun createDumps(): String = buildString {
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

/**
 * Creates a statement from the given SQL statement, runs the [block], commits the changes and closes the connection.
 *
 * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
 *
 * If [all connections are used][ConnectionSupplier.maxConnections],
 * this function suspends until a connection is available.
 *
 * The [block] should always be short-lived, consider using [transactional] otherwise.
 *
 * Supports nesting, but it is not recommended doing so,
 * avoid nesting by returning the data as soon as possible.
 *
 * @param sql           An SQL statement that may contain one or more '?' IN parameter placeholders
 * @param readOnly      `true` if the database only is read from, can allow some optimizations
 *                      but does **not** prevent writing
 * @param generatedKeys `true` to return the generated keys
 *
 * @see Database.fetchConnection
 * @see Database.transactional
 */
@Throws(SQLException::class)
@Suppress("SqlSourceToSinkFlow")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, generatedKeys: Boolean = false, block: SuspendingPreparedStatement.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withReusedConnection(readOnly) {
        SuspendingPreparedStatement(it.prepareStatement(
            sql,
            if (generatedKeys) Statement.RETURN_GENERATED_KEYS else Statement.NO_GENERATED_KEYS
        )).use(block)
    }
}

/**
 * Creates a statement from the given SQL statement, runs the [block], commits the changes and closes the connection.
 *
 * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
 *
 * If [all connections are used][ConnectionSupplier.maxConnections],
 * this function suspends until a connection is available.
 *
 * The [block] should always be short-lived, consider using [transactional] otherwise.
 *
 * Supports nesting, but it is not recommended doing so,
 * avoid nesting by returning the data as soon as possible.
 *
 * @param sql           An SQL statement that may contain one or more '?' IN parameter placeholders
 * @param readOnly      `true` if the database only is read from, can allow some optimizations
 *                      but does **not** prevent writing
 * @param columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows
 *
 * @see Database.fetchConnection
 * @see Database.transactional
 */
@Throws(SQLException::class)
@Suppress("SqlSourceToSinkFlow")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, columnIndexes: IntArray, block: SuspendingPreparedStatement.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withReusedConnection(readOnly) {
        SuspendingPreparedStatement(it.prepareStatement(sql, columnIndexes)).use(block)
    }
}

/**
 * Creates a statement from the given SQL statement, runs the [block], commits the changes and closes the connection.
 *
 * The returned keys are accessible using [getGeneratedKeys][AbstractPreparedStatement.getGeneratedKeys].
 *
 * If [all connections are used][ConnectionSupplier.maxConnections],
 * this function suspends until a connection is available.
 *
 * The [block] should always be short-lived, consider using [transactional] otherwise.
 *
 * Supports nesting, but it is not recommended doing so,
 * avoid nesting by returning the data as soon as possible.
 *
 * @param sql          An SQL statement that may contain one or more '?' IN parameter placeholders
 * @param readOnly     `true` if the database only is read from, can allow some optimizations
 *                     but does **not** prevent writing
 * @param columnNames  An array of column names indicating the columns that should be returned from the inserted row or rows
 *
 * @see Database.fetchConnection
 * @see Database.transactional
 */
@Throws(SQLException::class)
@Suppress("SqlSourceToSinkFlow")
suspend inline fun <R> Database.preparedStatement(@Language("PostgreSQL") sql: String, readOnly: Boolean = false, columnNames: Array<out String>, block: SuspendingPreparedStatement.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withReusedConnection(readOnly) {
        SuspendingPreparedStatement(it.prepareStatement(sql, columnNames)).use(block)
    }
}

@PublishedApi
internal suspend inline fun <R> Database.withReusedConnection(readOnly: Boolean, block: (connection: Connection) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val connection = currentTransactionIfExists(readOnly)?.connection
    if (connection != null)
        return block(connection)
    return fetchConnection(readOnly).use(block)
}