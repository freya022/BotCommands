package io.github.freya022.botcommands.internal.utils;

import kotlin.jvm.internal.PropertyReference;
import kotlin.reflect.*;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KParameterImpl;
import kotlin.reflect.jvm.internal.KPropertyImpl;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("KotlinInternalInJava")
class ReflectionMetadataAccessor {
    static KCallable<?> getParameterCallable(KParameter parameter) {
        return ((KParameterImpl) parameter).getCallable();
    }

    @Nullable
    static KDeclarationContainer getDeclaringClass(KProperty<?> property) {
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
