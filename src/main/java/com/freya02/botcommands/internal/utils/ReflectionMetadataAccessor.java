package com.freya02.botcommands.internal.utils;

import kotlin.reflect.KCallable;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KFunctionImpl;
import kotlin.reflect.jvm.internal.KParameterImpl;
import org.jetbrains.annotations.NotNull;

public class ReflectionMetadataAccessor {
    @NotNull
    @SuppressWarnings("KotlinInternalInJava")
    public static KCallable<?> getParameterCallable(@NotNull KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }

    @NotNull
    @SuppressWarnings("KotlinInternalInJava")
    public static KClass<?> getFunctionDeclaringClass(@NotNull KFunction<?> function) {
        return (KClassImpl<?>) ((KFunctionImpl) function).getContainer();
    }
}
