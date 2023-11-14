package io.github.freya022.botcommands.api.core.db;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionFunction<R, E extends Exception> {
    R apply(Connection connection) throws E;
}
