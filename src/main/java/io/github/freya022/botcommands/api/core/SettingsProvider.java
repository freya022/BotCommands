package io.github.freya022.botcommands.api.core;

import io.github.freya022.botcommands.api.commands.CommandList;
import io.github.freya022.botcommands.api.commands.application.CommandDeclarationFilter;
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter;
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier;
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import io.github.freya022.botcommands.api.localization.DefaultMessages;
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider;
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider;
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Interface for settings requested by the framework, such as prefixes, guild locale or guild commands whitelist.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
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

    /**
     * Returns the list of guild commands usable in that Guild.
     *
     * @return A CommandList of this guild's commands
     *
     * @see CommandList#all()
     * @see CommandList#none()
     * @see CommandList#of(Collection)
     * @see CommandList#notOf(Collection)
     * @see CommandList#filter(Predicate)
     *
     * @deprecated Replaced by {@link DeclarationFilter @DeclarationFilter} with {@link CommandDeclarationFilter}
     */
    @NotNull
    @Deprecated
    default CommandList getGuildCommands(@NotNull Guild guild) {
        return CommandList.all();
    }

    /**
     * Returns the {@link Locale} of the given {@link Guild}, will be null for a global context
     * <br>This might be used for localization such as in default messages or application commands
     *
     * @param guild The target {@link Guild} to get the {@link Locale} from
     *
     * @return The {@link Locale} of the specified guild
     *
     * @see DefaultMessages
     *
     * @deprecated Replaced by {@link TextCommandLocaleProvider} / {@link UserLocaleProvider} / {@link GuildLocaleProvider}
     */
    @NotNull
    @Deprecated
    default DiscordLocale getLocale(@Nullable Guild guild) {
        if (guild != null) return guild.getLocale();

        //Discord default locale is US english
        return DiscordLocale.ENGLISH_US;
    }
}
