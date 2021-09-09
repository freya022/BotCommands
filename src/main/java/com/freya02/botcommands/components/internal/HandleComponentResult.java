package com.freya02.botcommands.components.internal;

import com.freya02.botcommands.components.ComponentErrorReason;
import org.jetbrains.annotations.Nullable;

public class HandleComponentResult {
	private final ComponentErrorReason errorReason;
	private final boolean shouldDelete;

	public HandleComponentResult(ComponentErrorReason errorReason, boolean shouldDelete) {
		this.errorReason = errorReason;
		this.shouldDelete = shouldDelete;
	}

	/**
	 * Whether additional resources (such as the components handler maps) should get cleaned up
	 */
	public boolean shouldDelete() {
		return shouldDelete;
	}

	@Nullable
	public ComponentErrorReason getErrorReason() {
		return errorReason;
	}
}
