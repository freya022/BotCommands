package com.freya02.botcommands.api;

import com.freya02.botcommands.api.builder.ExtensionsBuilder;

/**
 * Functional interface where you supply an object of the specified parameter type (here T not the Class{@literal <?>})<br>
 * You also receive the type of the command in which the constructor parameter is
 *
 * @param <T> Type of the constructor parameter
 * @see ExtensionsBuilder#registerConstructorParameter(Class, ConstructorParameterSupplier)
 */
public interface ConstructorParameterSupplier<T> {
	T get(Class<?> commandType);
}
