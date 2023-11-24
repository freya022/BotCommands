package io.github.freya022.botcommands.api.core.db

import java.sql.PreparedStatement
import java.sql.ResultSet

sealed class AbstractPreparedStatement(val preparedStatement: PreparedStatement) : PreparedStatement by preparedStatement {
    protected fun setParameters(params: Array<out Any?>) {
        params.forEachIndexed { i, param ->
            // Primitive arrays are not always supported, do it ourselves
            when (param) {
                is IntArray -> setArray(i + 1, connection.createArrayOf("int", param.toTypedArray()))
                is LongArray -> setArray(i + 1, connection.createArrayOf("bigint", param.toTypedArray()))
                is FloatArray -> setArray(i + 1, connection.createArrayOf("real", param.toTypedArray()))
                is DoubleArray -> setArray(i + 1, connection.createArrayOf("double", param.toTypedArray()))
                is ByteArray -> setBytes(i + 1, param)
                is ShortArray -> setArray(i + 1, connection.createArrayOf("smallint", param.toTypedArray()))
                is BooleanArray -> setArray(i + 1, connection.createArrayOf("bool", param.toTypedArray()))
                is CharArray -> setArray(i + 1, connection.createArrayOf("char", param.toTypedArray()))
                else -> setObject(i + 1, param)
            }
        }
    }

    override fun getResultSet(): DBResult = DBResult(preparedStatement.resultSet)
    override fun getGeneratedKeys(): DBResult = DBResult(preparedStatement.generatedKeys)

    // region execute

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun execute(sql: String?): Boolean = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun execute(sql: String?, columnNames: Array<out String>?): Boolean = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun execute(sql: String?, autoGeneratedKeys: Int): Boolean = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun execute(sql: String?, columnIndexes: IntArray?): Boolean = throw UnsupportedOperationException()

    // endregion

    // region executeUpdate

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(sql: String?): Int = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(sql: String?, columnIndexes: IntArray?): Int = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(sql: String?, autoGeneratedKeys: Int): Int = throw UnsupportedOperationException()

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(sql: String?, columnNames: Array<out String>?): Int = throw UnsupportedOperationException()

    // endregion

    // region executeQuery

    @Deprecated("This is never usable as this is a prepared statement", level = DeprecationLevel.HIDDEN)
    override fun executeQuery(sql: String?): ResultSet = throw UnsupportedOperationException()

    // endregion

    override fun toString(): String {
        return preparedStatement.toString()
    }
}