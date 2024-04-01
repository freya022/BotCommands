package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("SqlSourceToSinkFlow")
@IgnoreStackFrame // Due to TracedConnection
public record BlockingTransaction(@NotNull Connection connection) {
    /**
     * Creates a statement from the given SQL statement.
     *
     * <p>The returned statement <b>must</b> be closed, with a try-with-resource, for example.
     *
     * @param sql An SQL statement that may contain one or more '?' IN parameter placeholders
     */
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql) throws SQLException {
        return new BlockingPreparedStatement(connection.prepareStatement(sql));
    }

    /**
     * Creates a statement from the given SQL statement.
     *
     * <p>The returned statement <b>must</b> be closed, with a try-with-resource, for example.
     *
     * @param sql           An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnIndexes An array of column indexes indicating the columns that should be returned from the inserted row or rows
     */
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql,
                                                       int @NotNull [] columnIndexes) throws SQLException {
        return new BlockingPreparedStatement(connection.prepareStatement(sql, columnIndexes));
    }

    /**
     * Creates a statement from the given SQL statement.
     *
     * <p>The returned statement <b>must</b> be closed, with a try-with-resource, for example.
     *
     * @param sql         An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnNames An array of column names indicating the columns that should be returned from the inserted row or rows
     */
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql,
                                                       @NotNull String @NotNull [] columnNames) throws SQLException {
        return new BlockingPreparedStatement(connection.prepareStatement(sql, columnNames));
    }

    /**
     * Creates a statement from the given SQL statement, runs the function and closes the statement.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param statementFunction The function to run with the prepared statement
     */
    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        try (final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql))) {
            return statementFunction.apply(statement);
        }
    }

    /**
     * Creates a statement from the given SQL statement, runs the function and closes the statement.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnIndexes     An array of column indexes indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     */
    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql,
                                                    int @NotNull [] columnIndexes,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        try (final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql, columnIndexes))) {
            return statementFunction.apply(statement);
        }
    }

    /**
     * Creates a statement from the given SQL statement, runs the function and closes the statement.
     *
     * @param sql               An SQL statement that may contain one or more '?' IN parameter placeholders
     * @param columnNames       An array of column names indicating the columns that should be returned from the inserted row or rows
     * @param statementFunction The function to run with the prepared statement
     */
    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql,
                                                    @NotNull String @NotNull [] columnNames,
                                                    @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        try (final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql, columnNames))) {
            return statementFunction.apply(statement);
        }
    }
}
