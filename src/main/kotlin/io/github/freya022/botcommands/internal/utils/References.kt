package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal val KProperty<*>.reference: String
    get() {
        val callableReference = (this as? CallableReference)
            ?: throwInternal("Referenced field doesn't seem to be compiler generated, exact type: ${this::class}")
        return (callableReference.owner as KClass<*>).simpleNestedName + "." + this.name
    }

internal inline fun <reified A : Annotation> annotationRef(): String = "@${classRef<A>()}"

internal inline fun <reified A : Any> classRef(): String = A::class.java.simpleNestedName