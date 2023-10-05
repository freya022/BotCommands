package com.freya02.botcommands.api.core.service;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface where you supply an instance of the given class type (your command)
 *
 * @param <T> Type of the class to instantiate
 *
 * @see BServiceConfigBuilder#registerInstanceSupplier(Class, InstanceSupplier)
 */
public interface InstanceSupplier<T> {
    @NotNull
    T supply(@NotNull BContext context);
}
