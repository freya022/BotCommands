package io.github.freya022.botcommands.api.core.reflect

import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents a generic type [T].
 *
 * You must make a subclass with the desired type, for example, for a `List<String>`:
 * ```java
 * var typeToken = new KotlinTypeToken<List<String>>() {};
 * ```
 *
 * However, you cannot create a KotlinTypeToken from a type variable (a generic, if you prefer):
 * ```java
 * <T> void myMethod() {
 *     // Doesn't work!
 *     var typeToken = new KotlinTypeToken<T>() {};
 * }
 * ```
 *
 * In other cases, you can create instances for any type using the static factory methods.
 */
open class KotlinTypeToken<T> {

    /**
     * The Kotlin type represented by this instance.
     */
    val type: KType

    /**
     * The Java type represented by this instance.
     */
    val javaType: Type get() = type.javaType

    /**
     * The [KClass] instance representing the runtime class to which this type is erased to.
     */
    val kotlinErasure: KClass<*> get() = type.jvmErasure

    /**
     * The [Class] instance representing the runtime class to which this type is erased to.
     */
    val javaErasure: Class<*> get() = type.jvmErasure.java

    protected constructor() {
        this.type = this::class.supertypes
            // `this` is a subclass of this class, so there must be a supertype of our erasure
            .single { it.jvmErasure == KotlinTypeToken::class }
            // Get the T type of KotlinTypeToken
            .arguments[0].type!!

        requireNotNull(type.classifier) {
            "Cannot represent '${(this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]}' as a KotlinTypeToken"
        }
    }

    private constructor(type: KType) {
        this.type = type
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinTypeToken<*>) return false

        return type == other.type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return "KotlinTypeToken(type=$type)"
    }

    companion object {

        /**
         * Creates a [KotlinTypeToken] constructed by a [clazz], followed by its type arguments.
         *
         * This is typically needed when the type token cannot be created at compile time,
         * where instead, you would be creating a subclass of [KotlinTypeToken].
         *
         * ### Example
         * ```java
         * public static <V> KotlinTypeToken<Map<String, V>> createMapTypeToken(Class<V> valueType) {
         *     return (KotlinTypeToken<Map<String, V>>) KotlinTypeToken.ofClass(Map.class, List.of(String.class, valueType));
         * }
         * ```
         *
         * As you can see, this cannot guarantee type safety,
         * you must take care of passing the right number of arguments.
         */
        @JvmStatic
        @JvmOverloads
        fun ofClass(clazz: Class<*>, arguments: List<KotlinTypeToken<*>> = listOf()): KotlinTypeToken<*> =
            KotlinTypeToken<Any>(
                clazz.kotlin.createType(
                    arguments.map { KTypeProjection.invariant(it.type) },
                    false,
                    listOf()
                )
            )

        /**
         * Creates a [KotlinTypeToken] with the given [KClass].
         *
         * If the given class has type parameters, they will be replaced by star projections (`*`),
         * this is equivalent to [`clazz.startProjectedType`][KClass.starProjectedType].
         */
        @JvmStatic
        @SkipJavaReflectionOverload
        fun ofKClass(clazz: KClass<*>) = KotlinTypeToken<Any>(clazz.starProjectedType)

        /**
         * Creates a [KotlinTypeToken] with the given [KType], unchanged.
         */
        @JvmStatic
        @SkipJavaReflectionOverload
        fun ofType(type: KType) = KotlinTypeToken<Any>(type)
    }
}
