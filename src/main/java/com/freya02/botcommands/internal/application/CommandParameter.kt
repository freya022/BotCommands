package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.ParameterResolvers
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.isPrimitive
import com.freya02.botcommands.internal.isReallyOptional
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.StringUtils
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

@OptIn(ExperimentalStdlibApi::class)
abstract class CommandParameter<RESOLVER : Any>(
    resolverType: KClass<RESOLVER>,
    val parameter: KParameter,
    val boxedType: KType,
    val index: Int
) {
    protected val _resolver: RESOLVER?
    val resolver: RESOLVER
        get() = _resolver ?: throwInternal("Tried to use a resolver but it was not set")

    protected val _customResolver: CustomResolver?
    val customResolver: CustomResolver
        get() = _customResolver ?: throwInternal("Tried to use a custom resolver but it was not set")


    val isOptional = parameter.isReallyOptional
    val isPrimitive = parameter.isPrimitive

    val isOption: Boolean
        get() = _resolver != null

    protected abstract val optionAnnotations: List<KClass<out Annotation>>

    /**
     * Returns the list of annotations that must be resolvable with a [ParameterResolver]
     * <br></br>If an option is not annotated with one of these but is still a valid option, it is up to the handler to fill up the parameters accordingly, without use the resolver nor the custom resolver
     */
    protected open val resolvableAnnotations: List<KClass<out Annotation>>
        get() = optionAnnotations

    constructor(resolverType: KClass<RESOLVER>, parameter: KParameter, index: Int) : this(
        resolverType,
        parameter,
        parameter.type,
        index
    )

    init {
        val resolver = ParameterResolvers.of(ParameterType.ofType(boxedType))
        val allowedAnnotation = optionAnnotations
        val resolvableAnnotation = resolvableAnnotations
        if (allowedAnnotation.any { parameter.findAnnotations(it).isNotEmpty() }) { //If the parameter has at least one valid annotation
            //If the parameter is not resolvable, but is still an option, then let the handler put the values itself
            if (resolvableAnnotation.none { parameter.findAnnotations(it).isNotEmpty() }) {
                this._resolver = null
                this._customResolver = null
            } else {
                requireNotNull(resolver) { "Unknown interaction command option type: " + boxedType.jvmErasure.qualifiedName + " for target resolver " + resolverType.qualifiedName }
                require(resolverType.isSuperclassOf(resolver.type.jvmErasure)) { "Unsupported interaction command option type: " + boxedType.jvmErasure.qualifiedName + " for target resolver " + resolverType.qualifiedName }

                @Suppress("UNCHECKED_CAST")
                this._resolver = resolver as RESOLVER
                this._customResolver = null
            }
        } else {
            this._resolver = null
            if (resolver is CustomResolver) {
                _customResolver = resolver
            } else {
                throw IllegalArgumentException("Unsupported custom parameter: %s, did you forget to use %s on non-custom options ?".format(
                    boxedType.jvmErasure.qualifiedName,
                    StringUtils.naturalJoin(
                        "or",
                        allowedAnnotation.map { it.simpleName }))
                )
            }
        }
    }
}