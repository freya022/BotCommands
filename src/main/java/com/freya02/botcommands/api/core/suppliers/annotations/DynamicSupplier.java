package com.freya02.botcommands.api.core.suppliers.annotations;

import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ConditionalService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this method as a dynamic instance supplier.
 *
 * <p>All dynamic instance suppliers are executed until one returns a valid object.
 * <br>This means this method can return the object of the requested class, or null if it cannot.
 *
 * <p><b>Implementation instructions:</b>
 * <ul>
 *     <li>The method needs to be {@code public}</li>
 *     <li>The method's first parameter needs to be a {@link Class Class<?>}</li>
 *     <li>The method needs to return an object</li>
 *     <li>For Java users, the method needs to be {@code static}</li>
 *     <li>For Kotlin users, the method needs to be in the {@code companion object}</li>
 * </ul>
 *
 * @see BService
 * @see ConditionalService
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicSupplier { }
