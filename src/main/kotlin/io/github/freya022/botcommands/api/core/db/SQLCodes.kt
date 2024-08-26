package io.github.freya022.botcommands.api.core.db

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import java.sql.SQLException

@Deprecated("Not portable, will be removed, you can copy this if you use PostgreSQL")
@ScheduledForRemoval
const val UNIQUE_VIOLATION = "23505"

@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@Deprecated("Not portable, will be removed, you can copy this if you use PostgreSQL")
@ScheduledForRemoval
fun SQLException.isUniqueViolation(): Boolean {
    return sqlState == UNIQUE_VIOLATION
}