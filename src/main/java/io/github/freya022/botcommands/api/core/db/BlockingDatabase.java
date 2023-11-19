package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.api.core.config.BConfig;
import io.github.freya022.botcommands.api.core.db.annotations.IgnoreStackFrame;
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery;
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory;
import io.github.freya022.botcommands.api.core.service.ServiceStart;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.Connection;
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
 *     <li>{@link BConfig#getLogQueries()} is enabled,
 *         and the logger of the class that created the prepared statement has its {@code TRACE} logs enabled.</li>
 *     <li>{@link BConfig#getQueryLogThreshold()} is configured</li>
 * </ul>
 *
 * <p>The logged SQL statements will use the logger of the class that created the prepared statement.
 * <br>If a utility class creates statements, you should use {@link IgnoreStackFrame @IgnoreStackFrame},
 * which will instead take the logger of the class that called your utility class.
 * <br>You can also use {@link BlockingPreparedStatement#withLogger(Logger)} if you wish to use a different logger.
 *
 * @see Database
 * @see ParametrizedQueryFactory
 */
@BService(start = ServiceStart.LAZY)
@Dependencies(Database.class)
@IgnoreStackFrame
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
     * <p>If {@link BConfig#getDumpLongTransactions()} is enabled,
     * a coroutine dump ({@link BConfig#getDumpLongTransactions() if available}) and a thread dump will be done
     * if the transaction is longer than {@link ConnectionSupplier#getMaxTransactionDuration() the threshold}.
     *
     * @see #fetchConnection()
     * @see #withStatement(String, StatementFunction)
     *
     * @see BConfig#getDumpLongTransactions()
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
     * <p>If {@link BConfig#getDumpLongTransactions()} is enabled,
     * a coroutine dump ({@link BConfig#getDumpLongTransactions() if available}) and a thread dump will be done
     * if the transaction is longer than {@link ConnectionSupplier#getMaxTransactionDuration() the threshold}.
     *
     * @param readOnly {@code true} if the database only is read from, can allow some optimizations
     *
     * @see #fetchConnection(boolean)
     * @see #withStatement(String, boolean, StatementFunction)
     *
     * @see BConfig#getDumpLongTransactions()
     * @see ConnectionSupplier#getMaxTransactionDuration()
     */
    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    public <R, E extends Exception> R withTransaction(boolean readOnly, @NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, readOnly, transactionFunction);
    }

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
     * @param readOnly {@code true} if the database only is read from, can allow some optimizations
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
}
