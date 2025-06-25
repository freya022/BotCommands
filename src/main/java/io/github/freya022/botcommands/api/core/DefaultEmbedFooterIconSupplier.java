package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Interface for embed footer icons requested by {@link BaseCommandEvent#getDefaultIconStream()}.
 *
 * <p>Returns {@code null} by default.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface DefaultEmbedFooterIconSupplier {

    @Nullable
    InputStream get();
}
