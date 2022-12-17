package com.freya02.botcommands.internal.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.sql.PreparedStatement
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class KPreparedStatement(val database: Database, val preparedStatement: PreparedStatement): PreparedStatement by preparedStatement {
    private fun setParameters(params: Array<out Any?>) {
        for ((i, param) in params.withIndex()) {
            setObject(i + 1, param)
        }
    }

    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        val sqlStr: String = addParametersAndGetQuery(params)
        withLoggedQuery(sqlStr) { execute() }
    }

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        val sqlStr: String = addParametersAndGetQuery(params)
        withLoggedQuery(sqlStr) { executeUpdate() }
    }

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        val sqlStr: String = addParametersAndGetQuery(params)
        DBResult(withLoggedQuery(sqlStr) { executeQuery() })
    }

    private fun addParametersAndGetQuery(params: Array<out Any?>): String = when {
        database.config.logQueryParameters -> setParameters(params).let { this@KPreparedStatement.toSQLString() }
        else -> toSQLString().also { setParameters(params) }
    }

    @OptIn(ExperimentalTime::class)
    private inline fun <R> withLoggedQuery(sqlStr: String, block: () -> R): R {
        val timedValue = measureTimedValue {
            runCatching { block() }
        }

        val result = timedValue.value
        if (logger.isTraceEnabled) {
            val duration = timedValue.duration
            val prefix = if (result.isSuccess) "Ran" else "Failed"
            logger.trace("$prefix query in %.3f ms: $sqlStr".format(duration.inWholeNanoseconds / 1000000.0))
        }

        return result.getOrThrow()
    }

    private fun toSQLString(): String {
        val str = preparedStatement.toString()
        val indexMarker = " wrapping "
        val index = str.indexOf(indexMarker)

        return preparedStatement.toString().substring(index + indexMarker.length)
            .lines()
            .map {
                val endIndex = Regex("--(?!.*')").find(it)?.range?.start ?: it.length
                it.substring(0, endIndex)
            }
            .joinToString(" ") { it.trim() }
    }

    override fun toString(): String {
        return preparedStatement.toString()
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}