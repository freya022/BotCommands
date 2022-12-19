package com.freya02.botcommands.internal.utils;

import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.internal.KCallableImpl;
import kotlin.reflect.jvm.internal.KParameterImpl;
import org.jetbrains.annotations.NotNull;

public class ReflectionMetadataAccessor {
    @NotNull
    @SuppressWarnings("KotlinInternalInJava")
    public static KCallableImpl<?> getParameterCallable(@NotNull KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }
}
