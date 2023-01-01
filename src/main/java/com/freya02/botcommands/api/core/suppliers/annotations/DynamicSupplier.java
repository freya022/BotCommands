package com.freya02.botcommands.api.core.suppliers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this method as a dynamic instance supplier.
 * <br>This means this method can return the request object, or null if it cannot.
 *
 * <p>All dynamic instance suppliers are tested until one returns a valid object
 *
 * <p><b>Implementation instructions:</b>
 * <ul>
 *     <li>The method needs to be {@code public}</li>
 *     <li>The method's first parameter needs to be a {@link Class}</li>
 *     <li>The method needs to return an object</li>
 *     <li>For Java users, the method needs to be {@code static}</li>
 *     <li>For Kotlin users, the method needs to be in the {@code companion object}</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicSupplier { }
