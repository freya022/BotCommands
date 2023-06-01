package com.freya02.botcommands.api.core.service.annotations;

import com.freya02.botcommands.api.parameters.ParameterResolverFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this class as a {@link ParameterResolverFactory parameter resolver factory}.
 * <br>This is a specialization of {@link BService} for parameter resolver factories.
 *
 * <p><b>Requirement:</b> this must extend {@link ParameterResolverFactory}.
 *
 * @see ParameterResolverFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) //Read by ClassGraph
public @interface ResolverFactory { }