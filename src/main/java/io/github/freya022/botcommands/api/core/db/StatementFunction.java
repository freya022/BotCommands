package io.github.freya022.botcommands.api.core.db;

import java.sql.Connection;

/**
 * Functional interface for Java JDBC prepared statements.
 *
 * @param <R> Type of the returned object
 * @param <E> Type of the exception thrown
 *
 * @see BlockingDatabase#withStatement(String, boolean, StatementFunction)
 */
@FunctionalInterface
public interface StatementFunction<R, E extends Exception> {
    R apply(Connection connection) throws E;
}
