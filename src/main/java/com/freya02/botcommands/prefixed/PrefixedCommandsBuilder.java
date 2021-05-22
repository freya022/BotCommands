package com.freya02.botcommands.prefixed;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class PrefixedCommandsBuilder {
	private final BContextImpl context;

	public PrefixedCommandsBuilder(@NotNull BContextImpl context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public void processPrefixedCommand(Command command) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		context.addCommand(command.getInfo().getName(), command.getInfo().getAliases(), command);

		for (Class<?> subcommandClazz : command.getClass().getClasses()) {
			if (isSubcommand(subcommandClazz)) {
				final Command subcommand = getSubcommand((Class<? extends Command>) subcommandClazz, command);

				if (subcommand != null) {
					command.getInfo().addSubcommand(subcommand);
				}
			}
		}
	}

	private Command getSubcommand(Class<? extends Command> clazz, Command parent) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		if (!Modifier.isAbstract(clazz.getModifiers())) {
			boolean isInstantiable = Utils.isInstantiable(clazz);

			if (isInstantiable) {
				if (Modifier.isStatic(clazz.getModifiers())) { //Static inner class doesn't need declaring class's instance
					final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor(BContext.class);
					if (!constructor.canAccess(null))
						throw new IllegalStateException("Constructor " + constructor + " is not public");

					return constructor.newInstance(context);
				} else {
					final Constructor<? extends Command> constructor = clazz.getDeclaredConstructor(parent.getClass(), BContext.class);
					if (!constructor.canAccess(null))
						throw new IllegalStateException("Constructor " + constructor + " is not public");

					return constructor.newInstance(parent, context);
				}
			}
		}

		return null;
	}

	private boolean isSubcommand(Class<?> aClass) {
		return !Modifier.isAbstract(aClass.getModifiers())
				&& aClass.isAnnotationPresent(JdaCommand.class)
				&& Command.class.isAssignableFrom(aClass);
	}
}
