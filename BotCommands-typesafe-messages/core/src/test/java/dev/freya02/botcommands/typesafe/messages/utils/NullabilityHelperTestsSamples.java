package dev.freya02.botcommands.typesafe.messages.utils;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

interface TypeUseRuntimeVisible {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Nullable {}
}

interface TypeUseRuntimeInvisible {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.CLASS)
    @interface Nullable {}
}

interface ParameterRuntimeVisible {
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Nullable {}
}

interface ParameterRuntimeInvisible {
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.CLASS)
    @interface Nullable {}
}

interface NullabilityHelperTestsSamples {
    void typeUseRuntimeVisible(@TypeUseRuntimeVisible.Nullable @Nullable String bar);
    void typeUseRuntimeInvisible(int i, @TypeUseRuntimeInvisible.Nullable String bar);
    void parameterRuntimeVisible(@ParameterRuntimeVisible.Nullable String bar, Integer i);
    void parameterRuntimeInvisible(int i, Integer i2, @ParameterRuntimeInvisible.Nullable String bar);
}
