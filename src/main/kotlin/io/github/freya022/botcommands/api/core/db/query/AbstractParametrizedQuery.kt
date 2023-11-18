package io.github.freya022.botcommands.api.core.db.query

import java.sql.PreparedStatement
import java.time.*
import java.time.format.DateTimeFormatter
import java.sql.Date as SqlDate
import java.sql.Time as SqlTime
import java.sql.Timestamp as SqlTimestamp

private val commentRegex = Regex("""--(?!.* ')""")

abstract class AbstractParametrizedQuery(protected val preparedStatement: PreparedStatement) : ParametrizedQuery {
    protected open fun formatParameter(value: Any?): String {
        return when (value) {
            null -> "NULL"
            is String -> preparedStatement.enquoteLiteral(value)
            is Number -> value.toString()
            is Boolean -> value.toString()
            is SqlDate -> DateTimeFormatter.ISO_LOCAL_DATE.format(value.toLocalDate())
            is SqlTimestamp -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value.toLocalDateTime())
            is SqlTime -> DateTimeFormatter.ISO_LOCAL_TIME.format(value.toLocalTime())
            is LocalTime -> DateTimeFormatter.ISO_LOCAL_TIME.format(value)
            is LocalDate -> DateTimeFormatter.ISO_LOCAL_DATE.format(value)
            is OffsetTime -> DateTimeFormatter.ISO_OFFSET_TIME.format(value)
            is LocalDateTime -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)
            is OffsetDateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value)
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