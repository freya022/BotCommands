package com.freya02.botcommands.internal.core.db

import java.sql.SQLException

const val UNIQUE_VIOLATION = "23505"

fun SQLException.isUniqueViolation(): Boolean {
    return sqlState == UNIQUE_VIOLATION
}