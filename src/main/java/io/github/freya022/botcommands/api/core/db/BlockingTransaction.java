package io.github.freya022.botcommands.api.core.db;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("SqlSourceToSinkFlow")
public record BlockingTransaction(@NotNull Connection connection) {
    @NotNull
    public BlockingPreparedStatement preparedStatement(@Language("PostgreSQL") @NotNull String sql) throws SQLException {
        return new BlockingPreparedStatement(connection.prepareStatement(sql));
    }

    public <R, E extends Exception> R withStatement(@Language("PostgreSQL") @NotNull String sql, @NotNull StatementFunction<R, E> transactionFunction) throws SQLException, E {
        return transactionFunction.apply(preparedStatement(sql));
    }
}
