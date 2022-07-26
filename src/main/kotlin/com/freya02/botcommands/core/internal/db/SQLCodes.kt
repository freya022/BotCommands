package com.freya02.botcommands.core.internal.db

import java.sql.SQLException

const val UNIQUE_VIOLATION = "23505"

@Suppress("NOTHING_TO_INLINE")
inline fun SQLException.isUniqueViolation(): Boolean {
    return sqlState == UNIQUE_VIOLATION
}