package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.isConstructor
import io.github.freya022.botcommands.api.core.utils.isStatic
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
import kotlin.reflect.full.isSubclassOf
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
            throwUser("$this : Function needs to be public")
        }

        requireUser(this.isConstructor || !this.isStatic || this.declaringClass.isValue, this) {
            "Function must not be static"
        }

        return lock.withLock {
            reflectedMap.computeIfAbsent(this) {
                when (this) { //Try to match the original function
                    is CallableReference -> resolveReference(owner as KClass<*>) ?: throwInternal(this, "Unable to reflect function reference")
                    else -> this
                }
            } as KFunction<R>
        }
    }

    internal fun KFunction<*>.resolveReference(targetClass: KClass<*>): KFunction<Any?>? {
        if (this !is CallableReference)
            throwInternal("Cannot use ReflectionUtils#resolveReference on a ${this::class.simpleNestedName}")

        return targetClass.declaredMemberFunctions.findFunction(this)
            ?: targetClass.constructors.findFunction(this)
    }

    internal fun KProperty<*>.resolveReference(targetClass: KClass<*>): KProperty<*>? {
        return targetClass.declaredMemberProperties.find { it.name == this.name }
    }

    private fun Collection<KFunction<*>>.findFunction(callableReference: CallableReference): KFunction<*>? =
        this.find { kFunction ->
            if (kFunction.name != callableReference.name) return@find false
            if (kFunction.nonInstanceParameters.size != callableReference.nonInstanceParameters.size) return@find false

            return@find kFunction.nonInstanceParameters.zip(callableReference.nonInstanceParameters).all { (first, second) ->
                first.type == second.type
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
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE && !it.type.jvmErasure.isSubclassOf(Event::class) }

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

internal fun KParameter.findDeclarationName(): String =
    name ?: throwUser("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")

internal fun KParameter.findOptionName(): String =
    name?.toDiscordString() ?: throwUser("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")

internal val KFunction<*>.javaMethodInternal: Method
    get() = javaMethod ?: throwInternal(this, "Could not resolve Java method")

internal fun <T : Any> KClass<T>.createSingleton(): T {
    val instance = this.objectInstance
    if (instance != null)
        return instance
    val constructor = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
        ?: throwUser("Class ${this.simpleNestedName} must either be an object, or have a no-arg constructor (or have only default parameters)")

    return constructor.callBy(mapOf())
}

internal inline fun <reified T : Any> KClass<*>.superErasureAt(index: Int): KType {
    val interfaceType = allSupertypes.firstOrNull { it.jvmErasure == T::class }
        ?: throwInternal("Unable to find the supertype of ${this.simpleNestedName} extending ${T::class.simpleNestedName}")
    return interfaceType.arguments[index].type!!
}

internal inline fun <reified T : Any> KType.findErasureOfAt(index: Int): KType {
    if (this.jvmErasure == T::class) {
        return this.arguments[index].type!!
    }

    return this.jvmErasure.superErasureAt<T>(index)
}