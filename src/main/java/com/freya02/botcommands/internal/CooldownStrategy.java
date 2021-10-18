package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.CooldownScope;

import java.util.concurrent.TimeUnit;

public class CooldownStrategy {
	private final long cooldown;
	private final TimeUnit unit;
	private final CooldownScope scope;

	public CooldownStrategy(long cooldown, TimeUnit unit, CooldownScope scope) {
		this.cooldown = cooldown;
		this.unit = unit;
		this.scope = scope;
	}

	public long getCooldown() {
		return cooldown;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public CooldownScope getScope() {
		return scope;
	}
}
