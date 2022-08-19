package com.freya02.botcommands.internal.core.db

import java.sql.ResultSet
import java.sql.SQLException

typealias ResultFunction<R> = (DBResult) -> R

class DBResult internal constructor(resultSet: ResultSet) : Iterable<DBResult>, ResultSet by resultSet {
    override fun iterator(): Iterator<DBResult> = object : Iterator<DBResult> {
        override fun hasNext(): Boolean {
            return try {
                this@DBResult.next()
            } catch (e: SQLException) {
                throw RuntimeException("Unable to iterate the result set", e)
            }
        }

        override fun next(): DBResult {
            return this@DBResult
        }
    }

    inline operator fun <reified R> get(columnLabel: String): R = getObject(columnLabel, R::class.java)

    inline operator fun <reified R> get(columnIndex: Int): R = getObject(columnIndex, R::class.java)

    fun readOnce(): DBResult? = when {
        !next() -> null
        else -> this
    }

    fun <R> readOnce(resultFunction: ResultFunction<R>): R? = when {
        !next() -> null
        else -> resultFunction(this)
    }

    fun <R> transformEach(resultFunction: ResultFunction<R>): List<R> = map(resultFunction)
}