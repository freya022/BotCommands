package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.internal.core.db.traced.TracedConnection;
import io.github.freya022.botcommands.internal.core.db.traced.TracedConnectionKt;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("SqlSourceToSinkFlow")
public record BlockingTransaction(@NotNull Connection connection) {
    /**
     * Creates a statement from the given SQL statement.
     *
     * <p>The returned statement <b>must</b> be closed, with a try-with-resource, for example.
     */
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql) throws SQLException {
        final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql));
        if (connection.isWrapperFor(TracedConnection.class))
            PreparedStatementsKt.withLogger(statement, TracedConnectionKt.loggerFromCallStack(1));

        return statement;
    }

    /**
     * Creates a statement from the given SQL statement, runs the function and closes the statement.
     */
    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql, @NotNull StatementFunction<R, E> statementFunction) throws SQLException, E {
        // Do not make the call stack deeper
        try (final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql))) {
            if (connection.isWrapperFor(TracedConnection.class))
                PreparedStatementsKt.withLogger(statement, TracedConnectionKt.loggerFromCallStack(1));
            return statementFunction.apply(statement);
        }
    }
}
