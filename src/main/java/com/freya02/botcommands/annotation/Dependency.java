package com.freya02.botcommands.annotation;

import com.freya02.botcommands.CommandsBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * Annotation to mark a class field as a "dependency", the field will be set on command construction if the type of the field was registered with {@link CommandsBuilder#registerCommandDependency(Class, Supplier)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Dependency {}