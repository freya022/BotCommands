package io.github.freya022.botcommands.api.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement

class KPreparedStatement @PublishedApi internal constructor(
    preparedStatement: PreparedStatement
): AbstractSuspendingKPreparedStatement(preparedStatement) {
    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.execute()
    }

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.executeUpdate()
    }

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        setParameters(params)
        DBResult(preparedStatement.executeQuery())
    }
}
