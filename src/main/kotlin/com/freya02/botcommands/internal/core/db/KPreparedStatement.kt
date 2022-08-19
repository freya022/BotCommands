package com.freya02.botcommands.internal.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class KPreparedStatement(val database: Database, val preparedStatement: PreparedStatement): PreparedStatement by preparedStatement {
    private fun setParameters(vararg params: Any?) {
        for ((i, param) in params.withIndex()) {
            setObject(i + 1, param)
        }
    }

    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        setParameters(*params)
        withLoggedQuery { execute() }
    }

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        setParameters(*params)
        withLoggedQuery { executeUpdate() }
    }

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        setParameters(*params)
        DBResult(withLoggedQuery { executeQuery() })
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun <R> withLoggedQuery(block: suspend () -> R): R {
        val timedValue = measureTimedValue {
            runCatching { block() }
        }

        val result = timedValue.value
        val duration = timedValue.duration
        preparedStatement.connection.prepareStatement("insert into bc_statement_result (query, success, time_nanos) values (?, ?, ?)").use { statement ->
            statement.setString(1, this.toSQLString())
            statement.setInt(2, if (result.isFailure) 0 else 1)
            statement.setLong(3, duration.inWholeNanoseconds)
            statement.executeUpdate()
        }

        return result.getOrThrow()
    }

    private fun toSQLString(): String {
        val str = preparedStatement.toString()
        val indexMarker = " wrapping "
        val index = str.indexOf(indexMarker)

        return preparedStatement.toString().substring(index + indexMarker.length)
            .lines()
            .joinToString(" ") { it.trim() }
    }

    override fun toString(): String {
        return preparedStatement.toString()
    }
}