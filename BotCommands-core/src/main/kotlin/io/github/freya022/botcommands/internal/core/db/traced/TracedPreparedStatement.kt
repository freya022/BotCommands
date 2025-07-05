package io.github.freya022.botcommands.internal.core.db.traced

import io.github.freya022.botcommands.api.core.db.query.ParametrizedQuery
import io.github.oshai.kotlinlogging.KLogger
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

internal class TracedPreparedStatement internal constructor(
    private val preparedStatement: PreparedStatement,
    internal var logger: KLogger,
    private val parametrizedQuery: ParametrizedQuery,
    private val logQueries: Boolean,
    private val isQueryThresholdSet: Boolean,
    private val queryLogThreshold: Duration
) : PreparedStatement by preparedStatement {
    override fun execute(): Boolean = measureTime { preparedStatement.execute() }

    override fun executeQuery(): ResultSet = measureTime { preparedStatement.executeQuery() }

    override fun executeUpdate(): Int = measureTime { preparedStatement.executeUpdate() }

    private inline fun <R> measureTime(block: () -> R): R {
        val (result, duration) = measureTimedValue {
            runCatching { block() }
        }

        logTimings(result, duration)

        return result.getOrThrow()
    }

    private fun logTimings(result: Result<*>, duration: Duration) {
        val parametrizedQuery = parametrizedQuery.toSql()

        if (logQueries) {
            logger.trace {
                val prefix = if (result.isSuccess) "Ran" else "Failed"
                "$prefix query in ${duration.toString(DurationUnit.MILLISECONDS, 2)}: $parametrizedQuery"
            }
        }
        if (isQueryThresholdSet && duration > queryLogThreshold) {
            val prefix = if (result.isSuccess) "Ran" else "Failed"
            logger.warn { "$prefix query in ${duration.toString(DurationUnit.MILLISECONDS, 2)}: $parametrizedQuery" }
        }
    }

    override fun isWrapperFor(iface: Class<*>): Boolean =
        iface.isInstance(this) || preparedStatement.isWrapperFor(iface)

    override fun <T : Any> unwrap(iface: Class<T>): T = when {
        iface.isInstance(this) -> iface.cast(this)
        else -> preparedStatement.unwrap(iface)
    }

    override fun setNull(parameterIndex: Int, sqlType: Int) {
        parametrizedQuery.addValue(parameterIndex, null)
        preparedStatement.setNull(parameterIndex, sqlType)
    }

    override fun setNull(parameterIndex: Int, sqlType: Int, typeName: String?) {
        parametrizedQuery.addValue(parameterIndex, null)
        preparedStatement.setNull(parameterIndex, sqlType, typeName)
    }

    override fun setBoolean(parameterIndex: Int, x: Boolean) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBoolean(parameterIndex, x)
    }

    override fun setByte(parameterIndex: Int, x: Byte) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setByte(parameterIndex, x)
    }

    override fun setShort(parameterIndex: Int, x: Short) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setShort(parameterIndex, x)
    }

    override fun setInt(parameterIndex: Int, x: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setInt(parameterIndex, x)
    }

    override fun setLong(parameterIndex: Int, x: Long) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setLong(parameterIndex, x)
    }

    override fun setFloat(parameterIndex: Int, x: Float) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setFloat(parameterIndex, x)
    }

    override fun setDouble(parameterIndex: Int, x: Double) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setDouble(parameterIndex, x)
    }

    override fun setBigDecimal(parameterIndex: Int, x: BigDecimal?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBigDecimal(parameterIndex, x)
    }

    override fun setString(parameterIndex: Int, x: String?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setString(parameterIndex, x)
    }

    override fun setBytes(parameterIndex: Int, x: ByteArray?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBytes(parameterIndex, x)
    }

    override fun setDate(parameterIndex: Int, x: Date?, cal: Calendar?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setDate(parameterIndex, x, cal)
    }

    override fun setDate(parameterIndex: Int, x: Date?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setDate(parameterIndex, x)
    }

    override fun setTime(parameterIndex: Int, x: Time?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setTime(parameterIndex, x)
    }

    override fun setTime(parameterIndex: Int, x: Time?, cal: Calendar?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setTime(parameterIndex, x, cal)
    }

    override fun setTimestamp(parameterIndex: Int, x: Timestamp?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setTimestamp(parameterIndex, x)
    }

    override fun setTimestamp(parameterIndex: Int, x: Timestamp?, cal: Calendar?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setTimestamp(parameterIndex, x, cal)
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setAsciiStream(parameterIndex, x, length)
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setAsciiStream(parameterIndex, x, length)
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setAsciiStream(parameterIndex, x)
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBinaryStream(parameterIndex, x, length)
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBinaryStream(parameterIndex, x, length)
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBinaryStream(parameterIndex, x)
    }

    override fun clearParameters() {
        parametrizedQuery.clear()
        preparedStatement.clearParameters()
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setObject(parameterIndex, x, targetSqlType)
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int, scaleOrLength: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    override fun setObject(parameterIndex: Int, x: Any?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setObject(parameterIndex, x)
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: SQLType?, scaleOrLength: Int) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: SQLType?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setObject(parameterIndex, x, targetSqlType)
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Int) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setCharacterStream(parameterIndex, reader, length)
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setCharacterStream(parameterIndex, reader, length)
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setCharacterStream(parameterIndex, reader)
    }

    override fun setRef(parameterIndex: Int, x: Ref?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setRef(parameterIndex, x)
    }

    override fun setBlob(parameterIndex: Int, inputStream: InputStream?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, inputStream)
        preparedStatement.setBlob(parameterIndex, inputStream, length)
    }

    override fun setBlob(parameterIndex: Int, x: Blob?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setBlob(parameterIndex, x)
    }

    override fun setBlob(parameterIndex: Int, inputStream: InputStream?) {
        parametrizedQuery.addValue(parameterIndex, inputStream)
        preparedStatement.setBlob(parameterIndex, inputStream)
    }

    override fun setClob(parameterIndex: Int, reader: Reader?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setClob(parameterIndex, reader, length)
    }

    override fun setClob(parameterIndex: Int, x: Clob?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setClob(parameterIndex, x)
    }

    override fun setClob(parameterIndex: Int, reader: Reader?) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setClob(parameterIndex, reader)
    }

    override fun setArray(parameterIndex: Int, x: Array?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setArray(parameterIndex, x)
    }

    override fun setURL(parameterIndex: Int, x: URL?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setURL(parameterIndex, x)
    }

    override fun setRowId(parameterIndex: Int, x: RowId?) {
        parametrizedQuery.addValue(parameterIndex, x)
        preparedStatement.setRowId(parameterIndex, x)
    }

    override fun setNString(parameterIndex: Int, value: String?) {
        parametrizedQuery.addValue(parameterIndex, value)
        preparedStatement.setNString(parameterIndex, value)
    }

    override fun setNCharacterStream(parameterIndex: Int, value: Reader?) {
        parametrizedQuery.addValue(parameterIndex, value)
        preparedStatement.setNCharacterStream(parameterIndex, value)
    }

    override fun setNCharacterStream(parameterIndex: Int, value: Reader?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, value)
        preparedStatement.setNCharacterStream(parameterIndex, value, length)
    }

    override fun setNClob(parameterIndex: Int, value: NClob?) {
        parametrizedQuery.addValue(parameterIndex, value)
        preparedStatement.setNClob(parameterIndex, value)
    }

    override fun setNClob(parameterIndex: Int, reader: Reader?) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setNClob(parameterIndex, reader)
    }

    override fun setNClob(parameterIndex: Int, reader: Reader?, length: Long) {
        parametrizedQuery.addValue(parameterIndex, reader)
        preparedStatement.setNClob(parameterIndex, reader, length)
    }

    override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
        parametrizedQuery.addValue(parameterIndex, xmlObject)
        preparedStatement.setSQLXML(parameterIndex, xmlObject)
    }
}