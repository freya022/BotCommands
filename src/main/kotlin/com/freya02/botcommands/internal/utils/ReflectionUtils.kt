package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata.lineNumber
import com.freya02.botcommands.internal.utils.ReflectionMetadata.sourceFile
import net.dv8tion.jda.api.events.Event
import java.lang.reflect.Method
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

internal object ReflectionUtils {
    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    internal fun <R> KFunction<R>.reflectReference(): KFunction<R> {
        //Still allow internal modifiers as they should be reflectively accessible
        if (this.visibility != KVisibility.PUBLIC && this.visibility != KVisibility.INTERNAL) {
            //Cannot use KFunction#shortSignature as ReflectionMetadata doesn't read non-public methods
            throwUser("$this : Function needs to be public")
        }

        requireUser(this.isConstructor || !this.isStatic || this.declaringClass.isValue, this) {
            "Function must not be static"
        }

        synchronized(reflectedMap) {
            return reflectedMap.computeIfAbsent(this) {
                return@computeIfAbsent when (this) { //Try to match the original function
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

    internal val KFunction<*>.shortSignatureNoSrc: String
        get() {
            val declaringClassName = this.declaringClass.simpleNestedName
            val methodName = this.name
            val parameters = this.valueParameters.joinToString { it.type.simpleNestedName }
            return "$declaringClassName.$methodName($parameters)"
        }

    internal val KFunction<*>.shortSignature: String
        get() {
            val returnType = this.returnType.simpleNestedName
            val source = this.javaMethodOrConstructorOrNull.let { method ->
                return@let when {
                    method != null && this.lineNumber != 0 -> {
                        val sourceFile = method.declaringClass.sourceFile
                        val lineNumber = this.lineNumber

                        "$sourceFile:$lineNumber"
                    }
                    else -> "<no-source>"
                }
            }
            return "$shortSignatureNoSrc: $returnType ($source)"
        }

    internal val KProperty<*>.referenceString: String
        get() {
            val callableReference = (this as? CallableReference)
                ?: throwInternal("Referenced field doesn't seem to be compiler generated, exact type: ${this::class}")
            return (callableReference.owner as KClass<*>).simpleName + "#" + this.name
        }

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