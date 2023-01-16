package com.freya02.botcommands.api.core.suppliers.annotations;

import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ConditionalService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this method as the instance supplier for the current class.
 * <br>The annotated method will run if a service of the current class needs to be instantiated.
 *
 * <p><b>Implementation instructions:</b>
 * <ul>
 *     <li>The method needs to be {@code public}</li>
 *     <li>The method needs to return the type of the current class</li>
 *     <li>For Java users, the method needs to be {@code static}</li>
 *     <li>For Kotlin users, the method needs to be in the {@code companion object}</li>
 * </ul>
 *
 * @see BService
 * @see ConditionalService
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InstanceSupplier {}
