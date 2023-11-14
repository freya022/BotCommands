package io.github.freya022.botcommands.api.core.db;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@BService
public class BlockingDatabase {
    private final Database database;

    public BlockingDatabase(Database database) {
        this.database = database;
    }

    @NotNull
    public Connection fetchConnection() throws SQLException {
        return DatabaseKt.fetchConnectionJava(database);
    }

    @NotNull
    public Connection fetchConnection(boolean readOnly) throws SQLException {
        return DatabaseKt.fetchConnectionJava(database, readOnly);
    }

    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    @NotNull
    public <R, E extends Exception> R withTransaction(@NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, transactionFunction);
    }

    @SuppressWarnings("RedundantThrows") // Hack so checked exceptions in the lambda are thrown by this method instead
    @NotNull
    public <R, E extends Exception> R withTransaction(boolean readOnly, @NotNull TransactionFunction<R, E> transactionFunction) throws SQLException, E {
        return DatabaseKt.withTransactionJava(database, readOnly, transactionFunction);
    }
}
