package com.freya02.botcommands.api;

import com.freya02.botcommands.api.builder.ExtensionsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface where you supply an instance of the given class type
 *
 * @see ExtensionsBuilder#registerDynamicInstanceSupplier(DynamicInstanceSupplier)
 */
public interface DynamicInstanceSupplier {
	@Nullable
	<T> T supply(@NotNull BContext context, @NotNull Class<T> clazz);
}
