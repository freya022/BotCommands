package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.annotations.ConditionalUse
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata.lineNumber
import com.freya02.botcommands.internal.utils.ReflectionMetadata.sourceFile
import io.github.classgraph.ClassInfo
import mu.KotlinLogging
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

internal object ReflectionUtils {
    private val logger = KotlinLogging.logger { }

    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

    private val serviceAnnotations: List<KClass<out Annotation>> = listOf(BService::class, ConditionalService::class, InjectedService::class)
    private val loadableServiceAnnotations: List<KClass<out Annotation>> = listOf(BService::class, ConditionalService::class)
    private val serviceAnnotationNames: List<String> = serviceAnnotations.map { it.java.name }

    internal fun Method.asKFunction(): KFunction<*> {
        return this.kotlinFunction ?: throwInternal("Unable to get kotlin function from $this")
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <R> KFunction<R>.reflectReference(): KFunction<R> {
        //Still allow internal modifiers as they should be reflectively accessible
        if (this.visibility != KVisibility.PUBLIC && this.visibility != KVisibility.INTERNAL) {
            //Cannot use KFunction#shortSignature as ReflectionMetadata doesn't read non-public methods
            throwUser("$this : Function needs to be public")
        }

        if (this.isStatic) {
            throwUser(this, "Function must not be static")
        }

        synchronized(reflectedMap) {
            return reflectedMap.computeIfAbsent(this) {
                return@computeIfAbsent when (this) { //Try to match the original function
                    is CallableReference -> {
                        (owner as KClass<*>).declaredMemberFunctions.find {//Don't use bound receiver, might be null somehow
                            it.name == name
                                    && it.nonInstanceParameters.zip(nonInstanceParameters).all { param ->
                                param.first.name == param.second.name
                                        && param.first.type == param.second.type
                            }
                        } ?: throwInternal("Unable to reflect function reference: $this")
                    }

                    else -> this
                }
            } as KFunction<R>
        }
    }

    internal val KFunction<*>.nonInstanceParameters
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE }

    internal val KFunction<*>.shortSignatureNoSrc: String
        get() {
            val declaringClassName = this.javaMethod?.declaringClass?.simpleNestedName ?: "<no-java-method>"
            val methodName = this.name
            val parameters = this.valueParameters.joinToString { it.type.jvmErasure.java.simpleNestedName }
            return "$declaringClassName#$methodName($parameters)"
        }

    internal val KFunction<*>.shortSignature: String
        get() {
            val returnType = this.returnType.simpleName
            val source = this.javaMethod.let { method ->
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
            return (callableReference.owner as KClass<*>).java.simpleName + "#" + this.name
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

    internal val KFunction<*>.collectionElementType: KClass<*>?
        get() = returnType.collectionElementType?.jvmErasure

    internal val KParameter.collectionElementType: KType?
        get() = type.collectionElementType

    /** Everything but extensions, includes static methods */
    internal val KClass<*>.nonExtensionFunctions
        //Take everything except extension functions
        get() = declaredFunctions.filter { it.extensionReceiverParameter == null }

    /** Static methods and declared member functions of the companion object */
    internal val KClass<*>.staticAndCompanionDeclaredMemberFunctions
        get() = this.staticFunctions + (companionObject?.declaredMemberFunctions ?: emptyList())

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun isInstantiable(info: ClassInfo): Boolean {
        var canInstantiate = true
        for (methodInfo in info.methodInfo) {
            if (methodInfo.hasAnnotation(ConditionalUse::class.java)) {
                if (methodInfo.isStatic) {
                    val function = methodInfo.loadClassAndGetMethod().asKFunction()
                    if (function.parameters.isEmpty() && function.returnType.jvmErasure == Boolean::class) {
                        requireUser(function.isPublic, function) { "Method must be public" }
                        canInstantiate = function.call() as Boolean
                    } else {
                        logger.warn("Method ${info.simpleName}#${function.name} is annotated @ConditionalUse but does not have the correct signature (return boolean, no parameters)")
                    }
                } else {
                    logger.warn("Method ${info.simpleName}#${methodInfo.name} is annotated @ConditionalUse but is not static")
                }

                break
            }
        }

        return canInstantiate
    }

    internal fun ClassInfo.isService() = serviceAnnotationNames.any { this.hasAnnotation(it) }
    internal fun KClass<*>.isService() = serviceAnnotations.any { this.findAnnotations(it).isNotEmpty() }
    internal fun KClass<*>.isLoadableService() = loadableServiceAnnotations.any { this.findAnnotations(it).isNotEmpty() }

    /**
     * Returns `true` if there is 1 service annotation or less
     */
    internal fun KClass<*>.hasAtMostOneServiceAnnotation(): Boolean {
        var found = false
        annotations.forEach { annotation ->
            if (annotation.annotationClass in loadableServiceAnnotations) {
                if (found)
                    return false
                found = true
            }
        }
        return true
    }
}