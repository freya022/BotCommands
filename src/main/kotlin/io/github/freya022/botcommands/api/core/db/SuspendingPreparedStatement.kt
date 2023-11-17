package io.github.freya022.botcommands.api.core.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

class SuspendingPreparedStatement @PublishedApi internal constructor(
    preparedStatement: PreparedStatement
): AbstractPreparedStatement(preparedStatement) {
    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun execute(): Boolean = throw UnsupportedOperationException()

    suspend fun execute(vararg params: Any?): Boolean = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.execute()
    }

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(): Int = throw UnsupportedOperationException()

    suspend fun executeUpdate(vararg params: Any?): Int = withContext(Dispatchers.IO) {
        setParameters(params)
        preparedStatement.executeUpdate()
    }

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeQuery(): ResultSet = throw UnsupportedOperationException()

    suspend fun executeQuery(vararg params: Any?): DBResult = withContext(Dispatchers.IO) {
        setParameters(params)
        DBResult(preparedStatement.executeQuery())
    }
}
