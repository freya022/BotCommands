package dev.freya02.botcommands.method.accessors.internal.codegen.utils

import io.github.freya022.botcommands.method.accessors.internal.MethodAccessor
import java.lang.constant.ClassDesc
import kotlin.coroutines.Continuation
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal val CD_Continuation = ClassDesc.of(Continuation::class.java.name)
internal val CD_KCallable = ClassDesc.of(KCallable::class.java.name)
internal val CD_KFunction = ClassDesc.of(KFunction::class.java.name)
internal val CD_KParameter = ClassDesc.of(KParameter::class.java.name)

internal val CD_MethodAccessor = ClassDesc.of(MethodAccessor::class.java.name)
