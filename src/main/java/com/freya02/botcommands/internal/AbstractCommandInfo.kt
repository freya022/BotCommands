package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CooldownScope;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.builder.CommandBuilder;
import com.freya02.botcommands.internal.runner.MethodRunner;
import kotlin.reflect.KFunction;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @param <T> Command instance type
 */
public abstract class AbstractCommandInfo<T> extends Cooldownable implements ExecutableInteractionInfo {
	private final T instance;

	/** This is NOT localized */
	protected final CommandPath path;
	protected final boolean ownerRequired;
	protected final KFunction<?> commandMethod;

	protected final EnumSet<Permission> userPermissions;
	protected final EnumSet<Permission> botPermissions;

	private final NSFWState nsfwState;
	private final String commandId;
	private final MethodRunner methodRunner;

	protected AbstractCommandInfo(@NotNull BContext context,
	                              @NotNull CommandBuilder builder) {
		super(new CooldownStrategy(0, TimeUnit.SECONDS, CooldownScope.USER)); //TODO

		this.instance = null;

		this.path = builder.getPath();
		this.commandMethod = builder.getFunction();
		this.methodRunner = new MethodRunner() { //TODO replace
			@SuppressWarnings("unchecked")
			@Override
			public <R> void invoke(@NotNull Object[] args, Consumer<Throwable> throwableConsumer, ConsumerEx<R> successCallback) {
				try {
					final Object call = commandMethod.call(args);

					successCallback.accept((R) call);
				} catch (Throwable e) {
					throwableConsumer.accept(e);
				}
			}
		};
		this.ownerRequired = ownerRequired;
		this.commandId = commandId;
		this.nsfwState = nsfwState;
		this.userPermissions = userPermissions;
		this.botPermissions = botPermissions;
	}

	@Override
	@NotNull
	public T getInstance() {
		return instance;
	}

	@Override
	public KFunction<?> getMethod() {
		return commandMethod;
	}

	@Override
	@NotNull
	public MethodRunner getMethodRunner() {
		return methodRunner;
	}

	/** This is NOT localized */
	public CommandPath getPath() {
		return path;
	}

	public EnumSet<Permission> getUserPermissions() {
		return userPermissions;
	}

	public EnumSet<Permission> getBotPermissions() {
		return botPermissions;
	}

	public boolean isOwnerRequired() {
		return ownerRequired;
	}

	@Nullable
	public NSFWState getNSFWState() {
		return nsfwState;
	}

	@Nullable
	public String getCommandId() {
		return commandId;
	}
}
