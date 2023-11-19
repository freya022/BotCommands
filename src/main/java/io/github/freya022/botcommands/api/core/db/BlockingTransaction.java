package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.internal.core.db.traced.TracedConnection;
import io.github.freya022.botcommands.internal.core.db.traced.TracedConnectionKt;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("SqlSourceToSinkFlow")
public record BlockingTransaction(@NotNull Connection connection) {
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql) throws SQLException {
        final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql));
        if (connection.isWrapperFor(TracedConnection.class))
            PreparedStatementsKt.withLogger(statement, TracedConnectionKt.loggerFromCallStack(1));

        return statement;
    }

    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql, @NotNull StatementFunction<R, E> transactionFunction) throws SQLException, E {
        // Do not make the call stack deeper
        final BlockingPreparedStatement statement = new BlockingPreparedStatement(connection.prepareStatement(sql));

        return transactionFunction.apply(statement);
            if (connection.isWrapperFor(TracedConnection.class))
                PreparedStatementsKt.withLogger(statement, TracedConnectionKt.loggerFromCallStack(1));
    }
}
