package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import java.sql.ResultSet
import java.sql.SQLException

class DBResult internal constructor(resultSet: ResultSet) : Iterable<DBResult>, ResultSet by resultSet {
    override fun iterator(): Iterator<DBResult> = object : Iterator<DBResult> {
        private var hasNext: Boolean? = null

        override fun hasNext(): Boolean {
            // Return existing state if possible
            hasNext?.let { return it }

            try {
                val hasNext = this@DBResult.next()
                this.hasNext = hasNext
                return hasNext
            } catch (e: SQLException) {
                throw RuntimeException("Unable to iterate the result set", e)
            }
        }

        override fun next(): DBResult {
            if (hasNext != true) {
                throw NoSuchElementException()
            }
            hasNext = null

            return this@DBResult
        }
    }

    inline operator fun <reified R> get(columnLabel: String): R = when {
        R::class.isSubclassOf<List<*>>() -> (getArray(columnLabel).array as Array<*>).toList() as R
        R::class.java.isArray -> getArray(columnLabel).array as R
        else -> getObject(columnLabel, R::class.java)
    }

    inline operator fun <reified R> get(columnIndex: Int): R = when {
        R::class.isSubclassOf<List<*>>() -> (getArray(columnIndex).array as Array<*>).toList() as R
        else -> getObject(columnIndex, R::class.java)
    }

    inline fun <reified R> getOrNull(columnLabel: String): R? = get<R>(columnLabel).let { if (wasNull()) null else it }

    inline fun <reified R> getOrNull(columnIndex: Int): R? = get<R>(columnIndex).let { if (wasNull()) null else it }

    fun read(): DBResult = readOrNull() ?: throw NoSuchElementException("There are no elements in this result set")

    fun readOrNull(): DBResult? = when {
        !next() -> null
        else -> this
    }

    fun <R> readOrNull(resultFunction: (DBResult) -> R): R? = when {
        !next() -> null
        else -> resultFunction(this)
    }
}