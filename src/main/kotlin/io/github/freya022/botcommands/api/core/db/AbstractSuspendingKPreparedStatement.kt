package io.github.freya022.botcommands.api.core.db

import java.sql.PreparedStatement
import java.sql.ResultSet

sealed class AbstractSuspendingKPreparedStatement(preparedStatement: PreparedStatement) : AbstractKPreparedStatement(preparedStatement) {
    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun execute(): Boolean = throw UnsupportedOperationException()

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeUpdate(): Int = throw UnsupportedOperationException()

    @Deprecated("Use suspending version", level = DeprecationLevel.HIDDEN)
    override fun executeQuery(): ResultSet = throw UnsupportedOperationException()
}