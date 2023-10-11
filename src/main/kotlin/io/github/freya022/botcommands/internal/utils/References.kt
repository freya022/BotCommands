package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.sourceFile
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal val KProperty<*>.reference: String
    get() {
        val callableReference = (this as? CallableReference)
            ?: throwInternal("Referenced field doesn't seem to be compiler generated, exact type: ${this::class}")
        return (callableReference.owner as KClass<*>).simpleNestedName + "." + this.name
    }

internal val Class<*>.shortQualifiedReference: String
    get() = "$shortQualifiedName($sourceFile:0)"

internal val KClass<*>.shortQualifiedReference: String
    get() = this.java.shortQualifiedReference