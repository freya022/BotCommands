package io.github.freya022.botcommands.api.core.db.query

interface ParametrizedQuery {
    fun clear()
    fun addValue(index: Int, value: Any?)

    fun toSql(): String
}