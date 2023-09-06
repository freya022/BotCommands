package com.freya02.botcommands.api.core.db

import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.full.isSubclassOf

// TODO remove in alpha 6
@Deprecated("For removal")
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

    inline operator fun <reified R> get(columnLabel: String): R = when {
        R::class.isSubclassOf(List::class) -> (getArray(columnLabel).array as Array<*>).toList() as R
        R::class == Array::class -> getArray(columnLabel).array as R
        else -> getObject(columnLabel, R::class.java)
    }

    inline operator fun <reified R> get(columnIndex: Int): R = when {
        R::class.isSubclassOf(List::class) -> (getArray(columnIndex).array as Array<*>).toList() as R
        else -> getObject(columnIndex, R::class.java)
    }

    inline fun <reified R> getOrNull(columnLabel: String): R? = get<R>(columnLabel).let { if (wasNull()) null else it }

    inline fun <reified R> getOrNull(columnIndex: Int): R? = get<R>(columnIndex).let { if (wasNull()) null else it }

    fun read(): DBResult = readOrNull() ?: throw NoSuchElementException("There are no elements in this result set")

    // TODO remove in alpha 6
    @Deprecated("Replaced by readOrNull", ReplaceWith("readOrNull()"), DeprecationLevel.ERROR)
    fun readOnce(): DBResult? = readOrNull()

    fun readOrNull(): DBResult? = when {
        !next() -> null
        else -> this
    }

    // TODO remove in alpha 6
    @Deprecated("Replaced by readOrNull", ReplaceWith("readOrNull(resultFunction)"), DeprecationLevel.ERROR)
    fun <R> readOnce(resultFunction: (DBResult) -> R): R? = readOrNull(resultFunction)

    fun <R> readOrNull(resultFunction: (DBResult) -> R): R? = when {
        !next() -> null
        else -> resultFunction(this)
    }

    // TODO remove in alpha 6
    @Deprecated("Replaced by Iterable#map", ReplaceWith("map(resultFunction)"), DeprecationLevel.ERROR)
    fun <R> transformEach(resultFunction: (DBResult) -> R): List<R> = map(resultFunction)
}