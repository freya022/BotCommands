package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;

/**
 * Interface for settings requested by the framework, such as prefixes, guild locale or guild commands whitelist.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see InterfacedService @InterfacedService
 *
 * @deprecated For removal, all functions were deprecated
 */
@Deprecated(forRemoval = true)
@InterfacedService(acceptMultiple = false)
public interface SettingsProvider {
}
