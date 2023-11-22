package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.internal.utils.findErasureOfAt
import java.sql.ResultSet
import java.sql.SQLException
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

    @JvmSynthetic
    inline operator fun <reified R> get(columnLabel: String): R = get<R>(findColumn(columnLabel))

    @JvmSynthetic
    inline operator fun <reified R> get(columnIndex: Int): R = when {
        R::class.isSubclassOf<List<*>>() -> readList(columnIndex, typeOf<R>().findErasureOfAt<List<*>>(0).jvmErasure.java) as R
        R::class.java.isArray -> readArray(columnIndex, R::class.java.componentType) as R
        else -> getObject(columnIndex, R::class.java)
    }

    @PublishedApi
    internal fun readArray(columnIndex: Int, elementType: Class<*>): Array<*> {
        val list = readList(columnIndex, elementType)
        val array = java.lang.reflect.Array.newInstance(elementType, list.size) as Array<*>
        list.forEachIndexed { index, o -> java.lang.reflect.Array.set(array, index, o) }
        return array
    }

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

    private fun Class<*>.toBoxed(): Class<*> {
        if (!isPrimitive) return this
        if (this == Integer.TYPE) return Int::class.javaObjectType
        if (this == java.lang.Long.TYPE) return Long::class.javaObjectType
        if (this == java.lang.Boolean.TYPE) return Boolean::class.javaObjectType
        if (this == java.lang.Byte.TYPE) return Byte::class.javaObjectType
        if (this == Character.TYPE) return Char::class.javaObjectType
        if (this == java.lang.Float.TYPE) return Float::class.javaObjectType
        if (this == java.lang.Double.TYPE) return Double::class.javaObjectType
        if (this == java.lang.Short.TYPE) return Short::class.javaObjectType
        return this
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