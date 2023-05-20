package com.freya02.botcommands.internal.utils;

import kotlin.reflect.KCallable;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.internal.KParameterImpl;
import org.jetbrains.annotations.NotNull;

class ReflectionMetadataAccessor {
    @NotNull
    @SuppressWarnings("KotlinInternalInJava")
    static KCallable<?> getParameterCallable(@NotNull KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }
}
