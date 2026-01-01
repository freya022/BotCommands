package io.github.freya022.botcommands.internal.utils;

import kotlin.jvm.internal.PropertyReference;
import kotlin.reflect.*;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KDeclarationContainerImpl;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassKind;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@NullMarked
class ReflectionMetadataAccessor {
    private static final Class<?> kPropertyClass;
    private static final MethodHandle getCallable, getContainer;

    // TODO Move to 2.3 types when 2.3.10 gets released
    //  with the fix for https://youtrack.jetbrains.com/issue/KT-83361/KotlinReflectionInternalError-Type-parameter-not-found-0-on-super-types-with-Kotlin-2.3.0
    static {
        try {
            var lookup = MethodHandles.publicLookup();
            Class<?> kParameter;
            Class<?> tmpKPropertyClass;
            Class<?> kCallableClass;

            try {
                kParameter = Class.forName("kotlin.reflect.jvm.internal.ReflectKParameter");
                tmpKPropertyClass = Class.forName("kotlin.reflect.jvm.internal.ReflectKProperty");
                kCallableClass = Class.forName("kotlin.reflect.jvm.internal.ReflectKCallable");
            } catch (ClassNotFoundException e) {
                kParameter = Class.forName("kotlin.reflect.jvm.internal.KParameterImpl");
                tmpKPropertyClass = Class.forName("kotlin.reflect.jvm.internal.KPropertyImpl");
                kCallableClass = Class.forName("kotlin.reflect.jvm.internal.KCallableImpl");
            }

            kPropertyClass = tmpKPropertyClass;
            getCallable = lookup.findVirtual(kParameter, "getCallable", MethodType.methodType(kCallableClass));
            getContainer = lookup.findVirtual(kPropertyClass, "getContainer", MethodType.methodType(KDeclarationContainerImpl.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static KCallable<?> getParameterCallable(KParameter parameter) {
        try {
            return (KCallable<?>) getCallable.invoke(parameter);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    static KDeclarationContainer getDeclaringClass(KProperty<?> property) {
        if (kPropertyClass.isInstance(property)) {
            try {
                return (KDeclarationContainer) getContainer.invoke(property);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
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
