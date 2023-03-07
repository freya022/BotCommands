package com.freya02.botcommands.api.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.sql.PreparedStatement
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class KPreparedStatement @PublishedApi internal constructor(val database: Database, val preparedStatement: PreparedStatement): PreparedStatement by preparedStatement {
    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { execute() }
    }

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { executeUpdate() }
    }

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { DBResult(executeQuery()) }
    }

    @OptIn(ExperimentalTime::class)
    private inline fun <R> withLoggedParametrizedQuery(params: Array<out Any?>, block: () -> R): R {
        if (!database.config.logQueries || !logger.isTraceEnabled) {
            setParameters(params)
            return block()
        } else {
            val parametrizedQuery: String = configureAndGetParametrizedQuery(params)

            val timedValue = measureTimedValue {
                runCatching { block() }
            }

            val result = timedValue.value
            val duration = timedValue.duration
            val prefix = if (result.isSuccess) "Ran" else "Failed"
            logger.trace("$prefix query in ${duration.toString(DurationUnit.MILLISECONDS, 2)}: $parametrizedQuery")

            return result.getOrThrow()
        }
    }

    private fun configureAndGetParametrizedQuery(params: Array<out Any?>): String = when {
        database.config.logQueryParameters -> setParameters(params).let { this@KPreparedStatement.toSQLString() }
        else -> toSQLString().also { setParameters(params) }
    }

    private fun setParameters(params: Array<out Any?>) {
        for ((i, param) in params.withIndex()) {
            setObject(i + 1, param)
        }
    }

    private fun toSQLString(): String {
        val str = preparedStatement.toString()
        val indexMarker = " wrapping "
        val index = str.indexOf(indexMarker)

        return preparedStatement.toString().substring(index + indexMarker.length)
            .lines()
            .map {
                val endIndex = commentRegex.find(it)?.range?.start ?: it.length
                it.substring(0, endIndex)
            }
            .joinToString(" ") { it.trim() }
    }

    override fun toString(): String {
        return preparedStatement.toString()
    }

    companion object {
        private val logger = KotlinLogging.logger { }

        private val commentRegex = Regex("""--(?!.* ')""")
    }
}
