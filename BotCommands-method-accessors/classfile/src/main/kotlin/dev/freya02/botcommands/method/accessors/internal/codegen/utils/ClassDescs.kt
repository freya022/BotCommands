package dev.freya02.botcommands.method.accessors.internal.codegen.utils

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodAccessorContinuation
import java.lang.constant.ClassDesc
import kotlin.coroutines.Continuation
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal val CD_IllegalStateException = ClassDesc.of(IllegalStateException::class.java.name)

internal val CD_Unit = ClassDesc.of(Unit::class.java.name)
internal val CD_DefaultConstructorMarker = ClassDesc.of("kotlin.jvm.internal.DefaultConstructorMarker")
internal val CD_Continuation = ClassDesc.of(Continuation::class.java.name)
internal val CD_KCallable = ClassDesc.of(KCallable::class.java.name)
internal val CD_KFunction = ClassDesc.of(KFunction::class.java.name)
internal val CD_KParameter = ClassDesc.of(KParameter::class.java.name)
internal val CD_ResultKt = ClassDesc.of("kotlin.ResultKt")
internal val CD_IntrinsicsKt = ClassDesc.of("kotlin.coroutines.intrinsics.IntrinsicsKt")
internal val CD_DebugProbesKt = ClassDesc.of("kotlin.coroutines.jvm.internal.DebugProbesKt")

internal val CD_MethodAccessor = ClassDesc.of(MethodAccessor::class.java.name)
internal val CD_MethodAccessorContinuation = ClassDesc.of(MethodAccessorContinuation::class.java.name)
