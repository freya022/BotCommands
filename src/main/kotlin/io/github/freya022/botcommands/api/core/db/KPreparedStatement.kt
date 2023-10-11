package io.github.freya022.botcommands.api.core.db

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger { }
private val commentRegex = Regex("""--(?!.* ')""")

class KPreparedStatement @PublishedApi internal constructor(
    private val database: Database,
    preparedStatement: PreparedStatement
): AbstractKPreparedStatement(preparedStatement) {
    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { preparedStatement.execute() }
    }

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { preparedStatement.executeUpdate() }
    }

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        withLoggedParametrizedQuery(params) { DBResult(preparedStatement.executeQuery()) }
    }

    private inline fun <R> withLoggedParametrizedQuery(params: Array<out Any?>, block: () -> R): R {
        val isTraceLogEnabled = database.config.logQueries && logger.isTraceEnabled
        val isQueryThresholdSet = database.config.queryLogThreshold.isFinite() && database.config.queryLogThreshold.isPositive()
        if (!isTraceLogEnabled && !isQueryThresholdSet) {
            setParameters(params)
            return block()
        } else {
            val parametrizedQuery: String = configureAndGetParametrizedQuery(params)

            val timedValue = measureTimedValue {
                runCatching { block() }
            }

            val result = timedValue.value
            if (isTraceLogEnabled) {
                logger.trace {
                    val duration = timedValue.duration
                    val prefix = if (result.isSuccess) "Ran" else "Failed"
                    "$prefix query in ${duration.toString(DurationUnit.MILLISECONDS, 2)}: $parametrizedQuery"
                }
            }
            if (isQueryThresholdSet && timedValue.duration > database.config.queryLogThreshold) {
                val duration = timedValue.duration
                val prefix = if (result.isSuccess) "Ran" else "Failed"
                logger.warn("$prefix query in ${duration.toString(DurationUnit.MILLISECONDS, 2)}: $parametrizedQuery")
            }

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
}
