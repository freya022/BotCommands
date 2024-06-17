package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.toBoxed
import io.github.freya022.botcommands.internal.utils.findErasureOfAt
import io.github.freya022.botcommands.internal.utils.rethrow
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * Utility class to iterate over a [ResultSet],
 * with a few methods to use nullable objects instead of booleans.
 */
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
                e.rethrow("Unable to iterate the result set")
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

    fun stream(): Stream<DBResult> = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL or Spliterator.ORDERED),
        false
    )

    @JvmSynthetic
    inline operator fun <reified R> get(columnLabel: String): R = get<R>(findColumn(columnLabel))

    @JvmSynthetic
    inline operator fun <reified R> get(columnIndex: Int): R = when {
        R::class.isSubclassOf<List<*>>() -> readList(columnIndex, typeOf<R>().findErasureOfAt<List<*>>(0).jvmErasure.java) as R
        R::class.java.isArray -> readArray(columnIndex, R::class.java.componentType) as R
        else -> getObject(columnIndex, R::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    @PublishedApi
    internal fun readArray(columnIndex: Int, elementType: Class<*>): Array<*> {
        val list = readList(columnIndex, elementType)
        val array = java.lang.reflect.Array.newInstance(elementType, list.size) as Array<Any?>
        list.forEachIndexed { index, o -> array[index] = o }
        return array
    }

    @JvmSynthetic
    @PublishedApi
    internal fun readList(columnIndex: Int, elementType: Class<*>): List<*> {
        val boxedType = elementType.toBoxed()
        val list = arrayListOf<Any?>()
        val rs = getArray(columnIndex).resultSet
        while (rs.next()) {
            list += rs.getObject(2, boxedType)
        }
        return list
    }

    @JvmSynthetic
    inline fun <reified R> getOrNull(columnLabel: String): R? = get<R>(columnLabel).let { if (wasNull()) null else it }

    @JvmSynthetic
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

fun DBResult.getKotlinInstant(columnName: String): Instant =
    getTimestamp(columnName).toInstant().toKotlinInstant()

fun DBResult.getKotlinInstantOrNull(columnName: String): Instant? =
    getTimestamp(columnName)?.toInstant()?.toKotlinInstant()