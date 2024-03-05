package io.github.freya022.botcommands.internal.utils;

import kotlin.reflect.KCallable;
import kotlin.reflect.KClass;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KParameterImpl;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("KotlinInternalInJava")
class ReflectionMetadataAccessor {
    @NotNull
    static KCallable<?> getParameterCallable(@NotNull KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }

    static ClassKind getClassKind(KClass<?> kClass) {
        return ((KClassImpl<?>) kClass).getDescriptor().getKind();
    }
}
