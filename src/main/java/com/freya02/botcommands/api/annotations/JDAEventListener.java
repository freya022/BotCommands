package com.freya02.botcommands.api.annotations;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.builder.ExtensionsBuilder;
import com.freya02.botcommands.api.parameters.CustomResolverFunction;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

/**
 * Annotates a method as a JDA event listener, this provides events the same way as {@link AnnotatedEventManager} do.
 * <ul>
 *     <li>The method name can be anything</li>
 *     <li>The first parameter has to be the targeted JDA event, if it is not a JDA event then this will fail on runtime, when building the framework</li>
 *     <li>You can optionally add more parameters, these are similar to commands "custom parameters" and are added with {@link ExtensionsBuilder#registerCustomResolver(Class, CustomResolverFunction)}} which is found in {@link CommandsBuilder#extensionsBuilder(Consumer)}</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JDAEventListener {}
