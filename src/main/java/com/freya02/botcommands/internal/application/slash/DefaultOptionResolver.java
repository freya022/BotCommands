package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.DefaultValue;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DefaultOptionResolver {
	@Nullable
	DefaultValue resolve(@NotNull BContext context, @NotNull Guild guild);
}
