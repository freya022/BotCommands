package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.annotations.NSFW;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class NSFWState {
	private final boolean enabledInGuild;
	private final boolean enabledInDMs;

	private NSFWState(boolean guildEnabled, boolean dmEnabled) {
		this.enabledInGuild = guildEnabled;
		this.enabledInDMs = dmEnabled;

		if (!enabledInDMs && !enabledInGuild) throw new IllegalArgumentException("Cannot disable both guild and DMs NSFW, as it would disable the command permanently");
	}

	@Nullable
	public static NSFWState ofMethod(@NotNull Method commandMethod) {
		final NSFW nsfw = AnnotationUtils.getEffectiveAnnotation(commandMethod, NSFW.class);
		if (nsfw == null) return null;

		return new NSFWState(nsfw.guild(), nsfw.dm());
	}

	public boolean isEnabledInGuild() {
		return enabledInGuild;
	}

	public boolean isEnabledInDMs() {
		return enabledInDMs;
	}
}