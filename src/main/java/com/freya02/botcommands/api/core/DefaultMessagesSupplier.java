package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.DefaultMessages;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.InterfacedService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

/**
 * This interface serves as a supplier for {@link DefaultMessages} instances, given a {@link DiscordLocale}.
 *
 * <p>
 * <b>Usage:</b> Register your instance as a service with {@link BService}, and a {@link ServiceType} of {@link DefaultMessagesSupplier}.
 *
 * @see InterfacedService
 */
@InterfacedService
public interface DefaultMessagesSupplier {
    @NotNull
    DefaultMessages get(@NotNull DiscordLocale discordLocale);
}
