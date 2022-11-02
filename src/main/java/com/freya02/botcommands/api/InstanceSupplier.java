package com.freya02.botcommands.api;

import com.freya02.botcommands.api.core.config.BServiceConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface where you supply an instance of the given class type (your command)
 *
 * @param <T> Type of the class to instantiate
 * @see BServiceConfig#registerInstanceSupplier(Class, InstanceSupplier)
 */
public interface InstanceSupplier<T> {
	@NotNull
	T supply(@NotNull BContext context);
}
