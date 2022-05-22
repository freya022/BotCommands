package com.freya02.botcommands.internal.application

import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.internal.ApplicationOptionData
import com.freya02.botcommands.internal.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation

abstract class ApplicationCommandParameter<RESOLVER : Any>(
    resolverType: KClass<RESOLVER>,
    parameter: KParameter,
    boxedType: KType,
    index: Int
) : CommandParameter<RESOLVER>(resolverType, parameter, boxedType, index) {
    private val _applicationOptionData: ApplicationOptionData?
    val applicationOptionData: ApplicationOptionData
        get() = _applicationOptionData ?: throwInternal("Tried to use application option data but it was not set")

    constructor(resolverType: KClass<RESOLVER>, parameter: KParameter, index: Int) : this(
        resolverType,
        parameter,
        parameter.type,
        index
    )

    init {
        _applicationOptionData = when {
            parameter.hasAnnotation<AppOption>() -> ApplicationOptionData(parameter)
            else -> null
        }
    }

    override fun optionAnnotations() = listOf(AppOption::class)
}