package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.api.core.config.BConfig;
import io.github.freya022.botcommands.api.core.service.ServiceStart;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class to use connections given by the [ConnectionSupplier].
 */
@BService(start = ServiceStart.LAZY)
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
     * Acquires a database connection, runs the [block], commits the changes and closes the connection.
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
    @NotNull
    public <R, E extends Exception> R withTransaction(@NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, transactionFunction);
    }

    /**
     * Acquires a database connection, runs the [block], commits the changes and closes the connection.
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
    @NotNull
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
