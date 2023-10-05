package com.freya02.botcommands.api.core;

import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.localization.DefaultMessages;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

/**
 * This interface serves as a supplier for {@link DefaultMessages} instances, given a {@link DiscordLocale}.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface DefaultMessagesSupplier {
    @NotNull
    DefaultMessages get(@NotNull DiscordLocale discordLocale);
}
