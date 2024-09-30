package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    /**
     * Returns the list of prefix this Guild should use <br>
     * <b>If the returned list is null or empty, the global prefixes will be used</b>
     *
     * @return The list of prefixes
     *
     * @deprecated Replaced by {@link TextPrefixSupplier#getPrefixes(GuildMessageChannel)}
     */
    @Nullable
    @Deprecated
    default List<String> getPrefixes(@NotNull Guild guild) {
        return null;
    }
}
