package io.github.freya022.botcommands.api.core.db.query

private val commentRegex = Regex("""--(?!.* ')""")

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
     * @param index The index of the parameter, starts at 1
     * @param value `null`-able value of the parameter
     */
    fun addValue(index: Int, value: Any?)

    /**
     * Constructs the SQL query out of the previously registered parameters.
     */
    fun toSql(): String

    fun removeCommentsAndInline(sql: String): String {
        return sql.lines()
            .map {
                val endIndex = commentRegex.find(it)?.range?.start ?: it.length
                it.substring(0, endIndex)
            }
            .joinToString(" ") { it.trim() }
    }
}