package com.freya02.botcommands.api.builder;

import com.freya02.botcommands.api.ConstructorParameterSupplier;
import com.freya02.botcommands.api.InstanceSupplier;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.JDAEventListener;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.api.parameters.*;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.runner.MethodRunnerFactory;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExtensionsBuilder {
	private final BContextImpl context;

	public ExtensionsBuilder(BContextImpl context) {
		this.context = context;
	}

	/**
	 * Registers a parameter resolver, must have one or more of the 3 interfaces, {@link RegexParameterResolver}, {@link SlashParameterResolver} and {@link ComponentParameterResolver}
	 *
	 * @param resolver Your own ParameterResolver to register
	 * @return This builder for chaining convenience
	 */
	public ExtensionsBuilder registerParameterResolver(ParameterResolver resolver) {
		ParameterResolvers.register(resolver);

		return this;
	}

	/**
	 * Registers a constructor parameter supplier, this means that your commands can have the given parameter type in it's constructor, and it will be injected during instantiation
	 *
	 * @param <T>               Type of the parameter
	 * @param parameterType     The type of the parameter inside your constructor
	 * @param parameterSupplier The supplier for this parameter
	 * @return This builder for chaining convenience
	 */
	public <T> ExtensionsBuilder registerConstructorParameter(Class<T> parameterType, ConstructorParameterSupplier<T> parameterSupplier) {
		if (context.getParameterSupplier(parameterType) != null)
			throw new IllegalStateException("Parameter supplier already exists for parameter of type " + parameterType.getName());

		context.registerConstructorParameter(parameterType, parameterSupplier);

		return this;
	}

	/**
	 * Registers a instance supplier, this means that your commands can be instantiated using the given {@link InstanceSupplier}<br><br>
	 * Instead of resolving the parameters manually with {@link #registerConstructorParameter(Class, ConstructorParameterSupplier)} you can use this to give directly the command's instance
	 *
	 * @param <T>              Type of the command's class
	 * @param classType        Type of the command's class
	 * @param instanceSupplier Instance supplier for this command
	 * @return This builder for chaining convenience
	 */
	public <T> ExtensionsBuilder registerInstanceSupplier(Class<T> classType, InstanceSupplier<T> instanceSupplier) {
		if (context.getInstanceSupplier(classType) != null)
			throw new IllegalStateException("Instance supplier already exists for class " + classType.getName());

		context.registerInstanceSupplier(classType, instanceSupplier);

		return this;
	}

	/**
	 * Registers a command dependency supplier, the supplier will be used on every field of the same type in a command if annotated with {@link Dependency @Dependency}
	 *
	 * @param <T>       Type of the field's object
	 * @param fieldType Type of the field's object
	 * @param supplier  Field supplier for this type
	 * @return This builder for chaining convenience
	 */
	public <T> ExtensionsBuilder registerCommandDependency(Class<T> fieldType, Supplier<T> supplier) {
		if (context.getCommandDependency(fieldType) != null)
			throw new IllegalStateException("Command dependency already exists for fields of type " + fieldType.getName());

		context.registerCommandDependency(fieldType, supplier);

		return this;
	}

	/**
	 * Register a custom resolver for commands (components / app commands), as well as the {@link JDAEventListener framework's event listeners}
	 *
	 * @param parameterType Type of the parameter
	 * @param function      Supplier function, may receive interaction events of any type
	 * @param <T>           Type of the parameter
	 * @return This builder for chaining convenience
	 */
	public <T> ExtensionsBuilder registerCustomResolver(Class<T> parameterType, Function<Event, T> function) {
		if (ParameterResolvers.exists(parameterType))
			throw new IllegalStateException("Custom resolver already exists for parameters of type " + parameterType.getName());

		ParameterResolvers.register(new CustomResolver(parameterType, function));

		return this;
	}

	/**
	 * Registers an autocompletion transformer
	 * <br>If your autocompletion handler return a {@code List<YourObject>}, you will have to register an {@code AutocompletionTransformer<YourObject>}
	 *
	 * @param type                      Type of the List generic element type
	 * @param autocompletionTransformer The transformer which transforms a {@link List} element into a {@link Command.Choice choice}
	 * @param <T>                       Type of the List generic element type
	 * @return This builder for chaining convenience
	 */
	public <T> ExtensionsBuilder registerAutocompletionTransformer(Class<T> type, AutocompletionTransformer<T> autocompletionTransformer) {
		context.registerAutocompletionTransformer(type, autocompletionTransformer);

		return this;
	}

	/**
	 * Sets the method runner factory, which allows methods to be reflectively executed
	 * <br>A method runner factory is already set for pure java methods,
	 * but you can set a {@link com.freya02.botcommands.api.runner.KotlinMethodRunnerFactory} to support <code>suspend</code> functions
	 *
	 * @param factory The {@link MethodRunnerFactory} to set
	 * @return This builder for chaining convenience
	 */
	public ExtensionsBuilder setMethodRunnerFactory(@NotNull MethodRunnerFactory factory) {
		Checks.notNull(factory, "Method runner factory");

		context.setMethodRunnerFactory(factory);

		return this;
	}
}
