package com.freya02.botcommands;

/**
 * Functional interface where you supply an instance of the given class type (your command)
 *
 * @param <T> Type of the class to instantiate
 * @see CommandsBuilder#registerInstanceSupplier(Class, InstanceSupplier)
 */
public interface InstanceSupplier<T> {
	T get(BContext context);
}
