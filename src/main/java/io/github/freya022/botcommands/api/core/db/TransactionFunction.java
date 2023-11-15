package io.github.freya022.botcommands.api.core.db;

import java.sql.Connection;

/**
 * Functional interface for Java JDBC transactions.
 *
 * @param <R> Type of the returned object
 * @param <E> Type of the exception thrown
 *
 * @see BlockingDatabase#withTransaction(boolean, TransactionFunction)
 */
@FunctionalInterface
public interface TransactionFunction<R, E extends Exception> {
    R apply(Connection connection) throws E;
}
