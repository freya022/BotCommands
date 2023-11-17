package io.github.freya022.botcommands.api.core.db.query

/**
 * Represents an SQL query with parameters.
 *
 * This is used for logging purposes.
 *
 * @see ParametrizedQueryFactory
 */
interface ParametrizedQuery {
    /**
     * Removes all parameters.
     */
    fun clear()

    /**
     * Adds a parameter to the query.
     *
     * @param index the index of the parameter, starts at 1
     * @param value `null`-able value of the parameter
     */
    fun addValue(index: Int, value: Any?)

    /**
     * Constructs the SQL query out of the previously registered parameters.
     */
    fun toSql(): String
}