package com.freya02.botcommands.core.internal.db

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
        database.fetchConnection().prepareStatement("insert into ")

        return result.getOrThrow()
    }

    private fun toSQLString(): String {
        val str = preparedStatement.toString()
        val index = str.indexOf(" wrapping ")

        return preparedStatement.toString().substring(index)
            .lines()
            .joinToString(" ") { it.trim() }
    }

    override fun toString(): String {
        return preparedStatement.toString()
    }
}