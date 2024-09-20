package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.isStatic
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.javaMethodOrConstructor
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.Event
import java.lang.reflect.Method
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.*
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

internal object ReflectionUtils {
    private val lock = ReentrantLock()
    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    internal fun <R> KFunction<R>.reflectReference(): KFunction<R> {
        reflectedMap[this]?.let { return it as KFunction<R> }

        //Still allow internal modifiers as they should be reflectively accessible
        if (this.visibility != KVisibility.PUBLIC && this.visibility != KVisibility.INTERNAL) {
            //Cannot use KFunction#shortSignature as ReflectionMetadata doesn't read non-public methods
            throwArgument("$this : Function needs to be public")
        }

        return lock.withLock {
            reflectedMap.computeIfAbsent(this) {
                when (this) { //Try to match the original function
                    // This used to give CallableReference#owner,
                    // this function takes the owner if there is no receiver,
                    // i.e., it is functionally the same or better.
                    is CallableReference -> resolveBestReference()
                    else -> this
                }
            } as KFunction<R>
        }
    }

    /**
     * Reflects the pure [KFunction] out of this [CallableReference].
     *
     * - If the callable reference is [bound to an instance][CallableReference.getBoundReceiver],
     * that instance is used to find back the function.
     * - If the callable reference is not bound,
     * the [LHS][CallableReference.getOwner] is used to find back the function.
     */
    internal fun <R> KFunction<R>.resolveBestReference(): KFunction<R> {
        if (this !is CallableReference)
            throwInternal("Cannot use ReflectionUtils#resolveReference on a ${this::class.simpleNestedName}")

        val targetClass = if (this.boundReceiver === CallableReference.NO_RECEIVER) {
            if (isStatic) {
                @Suppress("UNCHECKED_CAST")
                return this.owner.members.filterIsInstance<KFunction<*>>()
                    .findFunction(this) as KFunction<R>?
                    ?: throwInternal("Could not find best reference of static function $this")
            } else {
                this.owner as? KClass<*>
                    ?: throwInternal("Owner of callable reference is not class: ${this.owner}")
            }
        } else {
            this.boundReceiver::class
        }

        return resolveReferenceOrNull(targetClass)
            ?: throwInternal("Could not find best reference in ${targetClass.qualifiedName} of function $this")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R> KFunction<R>.resolveReferenceOrNull(targetClass: KClass<*>): KFunction<R>? {
        if (this !is CallableReference)
            throwInternal("Cannot use ReflectionUtils#resolveReference on a ${this::class.simpleNestedName}")

        return targetClass.declaredMemberFunctions.findFunction(this) as KFunction<R>?
            ?: targetClass.constructors.findFunction(this) as KFunction<R>?
            // Superclass/interface recursion
            ?: targetClass.superclasses.firstNotNullOfOrNull { resolveReferenceOrNull(it) }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <V> KProperty<V>.resolveReference(targetClass: KClass<*>): KProperty<V>? {
        return targetClass.declaredMemberProperties.find { it.name == this.name } as KProperty<V>?
    }

    private fun Collection<KFunction<*>>.findFunction(callableReference: CallableReference): KFunction<*>? =
        this.find { superFunction ->
            if (superFunction.name != callableReference.name) return@find false
            val superParameters = superFunction.nonInstanceParameters
            val refParameters = callableReference.nonInstanceParameters
            if (superParameters.size != refParameters.size) return@find false

            return@find superParameters.zip(refParameters).all { (superParameter, refParameter) ->
                superParameter.type.jvmErasure.isSubclassOf(refParameter.type.jvmErasure)
            }
        }

    internal val KParameter.function: KFunction<*>
        get() {
            val callable = ReflectionMetadataAccessor.getParameterCallable(this)
            return callable as? KFunction<*>
                ?: throwInternal("Unable to get the function of a KParameter, callable is: $callable")
        }

    internal val KFunction<*>.declaringClass: KClass<*>
        get() = this.javaMethodOrConstructor.declaringClass.kotlin

//    internal val KFunction<*>.isJava
//        get() = !declaringClass.hasAnnotation<Metadata>()

    internal val KCallable<*>.nonInstanceParameters
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE }

    internal val KCallable<*>.nonEventParameters
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE && !it.type.jvmErasure.isSubclassOf<Event>() }

    private val trustedCollections = listOf(Collection::class, List::class, Set::class)

    internal val KType.collectionElementType: KType?
        get() {
            //Type is a trusted collection, such as the Java collections
            if (jvmErasure in trustedCollections) {
                return arguments.first().type
            }

            //Maybe a subtype of Collection
            val collectionType = jvmErasure.supertypes.find { it.jvmErasure == Collection::class } ?: return null
            return collectionType.arguments.first().type
        }

//    /** Everything but extensions, includes static methods */
//    internal val KClass<*>.nonExtensionFunctions
//        //Take everything except extension functions
//        get() = declaredFunctions.filter { it.extensionReceiverParameter == null }
//
//    /** Static methods and declared member functions of the companion object */
//    internal val KClass<*>.staticAndCompanionDeclaredMemberFunctions
//        get() = this.staticFunctions + (companionObject?.declaredMemberFunctions ?: emptyList())

    internal fun Method.asKFunction(): KFunction<*> {
        return this.kotlinFunction ?: throwInternal("Unable to get kotlin function from $this")
    }
}

internal val KClass<*>.kind: ClassKind
    get() = ReflectionMetadataAccessor.getClassKind(this)

internal val KClass<*>.isObject: Boolean
    get() = kind == ClassKind.OBJECT

internal fun KParameter.findDeclarationName(): String =
    name ?: throwArgument("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")

internal fun KParameter.findOptionName(): String =
    name?.toDiscordString() ?: throwArgument("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")

internal val KFunction<*>.javaMethodInternal: Method
    get() = javaMethod ?: throwInternal(this, "Could not resolve Java method")

@PublishedApi
internal inline fun <reified T : Any> KClass<*>.superErasureAt(index: Int): KType = superErasureAt(index, T::class)

@PublishedApi
internal fun KClass<*>.superErasureAt(index: Int, targetType: KClass<*>): KType {
    val interfaceType = allSupertypes.firstOrNull { it.jvmErasure == targetType }
        ?: throwInternal("Unable to find the supertype '${targetType.simpleNestedName}' in '${this.simpleNestedName}'")
    return interfaceType.arguments[index].type
        ?: throwArgument("Star projections are not allowed on argument #$index of ${targetType.simpleNestedName}")
}

@PublishedApi
internal inline fun <reified T : Any> KType.findErasureOfAt(index: Int): KType = findErasureOfAt(index, T::class)

@PublishedApi
internal fun KType.findErasureOfAt(index: Int, targetType: KClass<*>): KType {
    if (this.jvmErasure == targetType) {
        return this.arguments[index].type
            ?: throwArgument("Star projections are not allowed on argument #$index of ${targetType.simpleNestedName}")
    }

    return this.jvmErasure.superErasureAt(index, targetType)
}

internal fun KType.typeOfAtOrNullOnStar(index: Int, targetType: KClass<*>): KType? {
    if (this.jvmErasure == targetType) {
        return this.arguments[index].type
    }

    return this.jvmErasure.superErasureAt(index, targetType)
}

internal fun <T : Any> Class<T>.safeCast(instance: Any?): T? = when {
    isInstance(instance) -> cast(instance)
    else -> null
}
