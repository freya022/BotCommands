package io.github.freya022.botcommands.internal.utils;

import kotlin.jvm.internal.PropertyReference;
import kotlin.reflect.*;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KParameterImpl;
import kotlin.reflect.jvm.internal.KPropertyImpl;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("KotlinInternalInJava")
class ReflectionMetadataAccessor {
    @NotNull
    static KCallable<?> getParameterCallable(@NotNull KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }

    @Nullable
    static KDeclarationContainer getDeclaringClass(@NotNull KProperty<?> property) {
        if (property instanceof KPropertyImpl) {
            return ((KPropertyImpl<?>) property).getContainer();
        } else if (property instanceof PropertyReference) {
            return ((PropertyReference) property).getOwner();
        } else {
            return null;
        }
    }

    static ClassKind getClassKind(KClass<?> kClass) {
        return ((KClassImpl<?>) kClass).getDescriptor().getKind();
    }
}
