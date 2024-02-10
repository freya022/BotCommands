package io.github.freya022.botcommands.api.core.db.query

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.sql.PreparedStatement
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.sql.Array as SqlArray
import java.sql.Date as SqlDate
import java.sql.Time as SqlTime
import java.sql.Timestamp as SqlTimestamp

abstract class AbstractParametrizedQuery(protected val preparedStatement: PreparedStatement) : ParametrizedQuery {
    protected open fun formatParameter(value: Any?): String {
        if (value == null) return "NULL"
        if (value.javaClass.isArray) {
            return "ARRAY" + when (value) {
                is ByteArray -> value.contentToString()
                is ShortArray -> value.contentToString()
                is IntArray -> value.contentToString()
                is LongArray -> value.contentToString()
                is FloatArray -> value.contentToString()
                is DoubleArray -> value.contentToString()
                is BooleanArray -> value.contentToString()
                is CharArray -> value.joinToString(prefix = "[", postfix = "]") { preparedStatement.enquoteLiteral(it.toString()) }
                is Array<*> -> value.joinToString(prefix = "[", postfix = "]") { formatParameter(it) }
                else -> throwInternal("Unsupported array type: ${value.javaClass.simpleNestedName}")
            }
        }
        return when (value) {
            is String -> preparedStatement.enquoteLiteral(value)
            is Number -> value.toString()
            is Char -> preparedStatement.enquoteLiteral(value.toString())
            is Boolean -> value.toString()
            is SqlDate -> DateTimeFormatter.ISO_LOCAL_DATE.formatSqlDate(value.toLocalDate())
            is SqlTimestamp -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.formatSqlDate(value.toLocalDateTime())
            is SqlTime -> DateTimeFormatter.ISO_LOCAL_TIME.formatSqlDate(value.toLocalTime())
            is LocalTime -> DateTimeFormatter.ISO_LOCAL_TIME.formatSqlDate(value)
            is LocalDate -> DateTimeFormatter.ISO_LOCAL_DATE.formatSqlDate(value)
            is OffsetTime -> DateTimeFormatter.ISO_OFFSET_TIME.formatSqlDate(value)
            is LocalDateTime -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.formatSqlDate(value)
            is OffsetDateTime -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.formatSqlDate(value)
            is SqlArray -> formatParameter(value.array)
            else -> value.toString()
        }
    }

    private fun DateTimeFormatter.formatSqlDate(temporal: TemporalAccessor) =
        preparedStatement.enquoteLiteral(format(temporal))
}