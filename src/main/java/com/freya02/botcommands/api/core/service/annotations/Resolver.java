package com.freya02.botcommands.api.core.service.annotations;

import com.freya02.botcommands.api.parameters.ParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this class as a {@link ParameterResolver parameter resolver}.
 * <br>This is a specialization of {@link BService} for parameter resolvers.
 *
 * <p><b>Requirement:</b> this must extend {@link ParameterResolver}.
 *
 * @see BService
 *
 * @see ParameterResolver
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) //Read by ClassGraph
public @interface Resolver { }