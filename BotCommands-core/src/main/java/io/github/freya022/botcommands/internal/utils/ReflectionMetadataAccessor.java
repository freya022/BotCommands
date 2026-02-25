package io.github.freya022.botcommands.internal.utils;

import kotlin.jvm.internal.PropertyReference;
import kotlin.reflect.*;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.ReflectKParameter;
import kotlin.reflect.jvm.internal.ReflectKProperty;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ReflectionMetadataAccessor {
    static KCallable<?> getParameterCallable(KParameter parameter) {
        return ((ReflectKParameter) parameter).getCallable();
    }

    @Nullable
    static KDeclarationContainer getDeclaringClass(KProperty<?> property) {
        if (property instanceof ReflectKProperty<?>) {
            return ((ReflectKProperty<?>) property).getContainer();
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
