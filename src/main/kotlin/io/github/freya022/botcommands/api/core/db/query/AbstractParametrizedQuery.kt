package io.github.freya022.botcommands.api.core.db.query

import java.sql.PreparedStatement
import java.text.SimpleDateFormat

private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
private val commentRegex = Regex("""--(?!.* ')""")

abstract class AbstractParametrizedQuery(protected val preparedStatement: PreparedStatement) : ParametrizedQuery {
    protected open fun formatParameter(value: Any?): String {
        return when (value) {
            null -> "NULL"
            is String -> preparedStatement.enquoteLiteral(value)
            is Number -> value.toString()
            is Boolean -> value.toString()
            is java.util.Date -> dateFormat.format(value)
            else -> value.toString()
        }
    }

    protected open fun removeComments(sql: String): String {
        return sql.lines()
            .map {
                val endIndex = commentRegex.find(it)?.range?.start ?: it.length
                it.substring(0, endIndex)
            }
            .joinToString(" ") { it.trim() }
    }
}