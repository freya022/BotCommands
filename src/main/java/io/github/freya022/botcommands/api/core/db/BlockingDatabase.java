package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame;
import io.github.freya022.botcommands.api.core.config.BDatabaseConfig;
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase;
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery;
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.Lazy;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility class to use connections given by the {@link ConnectionSupplier}.
 *
 * <p>Use {@link Database} if you use Kotlin.
 *
 * <h3>Tracing</h3>
 * The connection could be wrapped depending on the configuration, for example,
 * to log the queries (in which case a {@link ParametrizedQuery} is used), as well as timing them.
 *
 * <p>A SQL statement is traced if any of these conditions is met:
 * <ul>
 *     <li>{@link BDatabaseConfig#getLogQueries()} is enabled,
 *         and the logger of the class that created the prepared statement has its {@code TRACE} logs enabled.</li>
 *     <li>{@link BDatabaseConfig#getQueryLogThreshold()} is configured</li>
 * </ul>
 *
 * <p>The logged SQL statements will use the logger of the class that created the prepared statement.
 * <br>If a utility class creates statements, you should use {@link IgnoreStackFrame @IgnoreStackFrame},
 * which will instead take the logger of the class that called your utility class.
 * <br>You can also use {@link BlockingPreparedStatement#withLogger(Logger)} if you wish to use a different logger.
 *
 * <h3>Batching support</h3>
 * If you must run a lot of DML statements ({@code INSERT}, {@code UPDATE}, ...),
 * you can batch them as to execute all of them in one go, massively improving performances on larger updates.
 *
 * <p>For that, you can use any function giving you a {@link BlockingPreparedStatement prepared statement}, then,
 * you can add statements by:
 * <ul>
 *     <li>Adding the parameters using {@link BlockingPreparedStatement#setParameters(Object[])}</li>
 *     <li>Calling {@link PreparedStatement#addBatch() BlockingPreparedStatement#addBatch()}</li>
 * </ul>
 *
 * <p>Repeat those two steps for all your statements,
 * then call {@link BlockingPreparedStatement#executeBatch()} to run all of them.
 *
 * <p><b>Note:</b> To read returned columns (like an {@code INSERT INTO ... RETURNING {column}} in PostgreSQL),
 * you must specify the column indexes/names when creating your statement,
 * and read them back from {@link SuspendingPreparedStatement#getGeneratedKeys()}.
 *
 * @see RequiresDatabase @RequiresDatabase
 * @see Database
 * @see ParametrizedQueryFactory
 */
@Lazy
@BService
@RequiresDatabase
@IgnoreStackFrame // Due to TracedConnection
public class BlockingDatabase {
    private final Database database;

    public BlockingDatabase(Database database) {
        this.database = database;
    }

    /**
     * Acquires a database connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The connection should always be short-lived,
     * consider using {@link #withTransaction(boolean, TransactionFunction)} otherwise.
     *
     * <p>The returned connection <b>must</b> be closed, with a try-with-resource, for example.
     *
     * @see #withTransaction(boolean, TransactionFunction)
     * @see #withStatement(String, StatementFunction)
     */
    @NotNull
    public Connection fetchConnection() throws SQLException {
        return DatabaseKt.fetchConnectionJava(database);
    }

    /**
     * Acquires a database connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The connection should always be short-lived,
     * consider using {@link #withTransaction(boolean, TransactionFunction)} otherwise.
     *
     * <p>The returned connection <b>must</b> be closed, with a try-with-resource, for example.
     *
     * @param readOnly {@code true} if the database only is read from, can allow some optimizations
     *
     * @see #withTransaction(boolean, TransactionFunction)
     * @see #withStatement(String, boolean, StatementFunction)
     */
    @NotNull
    public Connection fetchConnection(boolean readOnly) throws SQLException {
        return DatabaseKt.fetchConnectionJava(database, readOnly);
    }

    /**
     * Acquires a database connection, runs the function, commits the changes and closes the connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>If {@link BDatabaseConfig#getDumpLongTransactions()} is enabled,
     * a coroutine dump ({@link BDatabaseConfig#getDumpLongTransactions() if available}) and a thread dump will be done
     * if the transaction is longer than {@link ConnectionSupplier#getMaxTransactionDuration() the threshold}.
     *
     * @param transactionFunction The function to run with the connection
     *
     * @see #fetchConnection()
     * @see #withStatement(String, StatementFunction)
     *
     * @see BDatabaseConfig#getDumpLongTransactions()
     * @see ConnectionSupplier#getMaxTransactionDuration()
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withTransaction(@NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, transactionFunction);
    }

    /**
     * Acquires a database connection, runs the function, commits the changes and closes the connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>If {@link BDatabaseConfig#getDumpLongTransactions()} is enabled,
     * a coroutine dump ({@link BDatabaseConfig#getDumpLongTransactions() if available}) and a thread dump will be done
     * if the transaction is longer than {@link ConnectionSupplier#getMaxTransactionDuration() the threshold}.
     *
     * @param readOnly            {@code true} if the database only is read from, can allow some optimizations
     * @param transactionFunction The function to run with the connection
     *
     * @see #fetchConnection(boolean)
     * @see #withStatement(String, boolean, StatementFunction)
     *
     * @see BDatabaseConfig#getDumpLongTransactions()
     * @see ConnectionSupplier#getMaxTransactionDuration()
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withTransaction(boolean readOnly, @NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, readOnly, transactionFunction);
    }

    // NOTE: Language injection does not work due to https://youtrack.jetbrains.com/issue/IDEA-341219
    // But this does not impact the library at all, only the DX

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection()
     * @see #withTransaction(TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, statementFunction);
    }

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(boolean, TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param readOnly          {@code true} if the database only is read from, can allow some optimizations
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection(boolean)
     * @see #withTransaction(boolean, TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    boolean readOnly,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, readOnly, statementFunction);
    }

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>The returned keys are accessible using {@link AbstractPreparedStatement#getGeneratedKeys() getGeneratedKeys()}.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnIndexes     An array of column index indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection()
     * @see #withTransaction(TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    int @NotNull [] columnIndexes,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, columnIndexes, statementFunction);
    }

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>The returned keys are accessible using {@link AbstractPreparedStatement#getGeneratedKeys() getGeneratedKeys()}.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(boolean, TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param readOnly          {@code true} if the database only is read from, can allow some optimizations
     * @param columnIndexes     An array of column index indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection(boolean)
     * @see #withTransaction(boolean, TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    boolean readOnly,
                                                    int @NotNull [] columnIndexes,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, readOnly, columnIndexes, statementFunction);
    }

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>The returned keys are accessible using {@link AbstractPreparedStatement#getGeneratedKeys() getGeneratedKeys()}.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnNames       An array of column names indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection()
     * @see #withTransaction(TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    @NotNull String @NotNull [] columnNames,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, columnNames, statementFunction);
    }

    /**
     * Creates a statement from the given SQL statement, runs the function,
     * commits the changes and closes the connection.
     *
     * <p>The returned keys are accessible using {@link AbstractPreparedStatement#getGeneratedKeys() getGeneratedKeys()}.
     *
     * <p>If {@link ConnectionSupplier#getMaxConnections() all connections are used},
     * this function blocks until a connection is available.
     *
     * <p>The function should always be short-lived,
     * consider using {@link #withTransaction(boolean, TransactionFunction)} otherwise.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param readOnly          {@code true} if the database only is read from, can allow some optimizations
     * @param columnNames       An array of column names indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     *
     * @see #fetchConnection(boolean)
     * @see #withTransaction(boolean, TransactionFunction)
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withStatement(@NotNull @Language("PostgreSQL") String sql,
                                                    boolean readOnly,
                                                    @NotNull String @NotNull [] columnNames,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        return DatabaseKt.withStatementJava(database, sql, readOnly, columnNames, statementFunction);
    }
}
