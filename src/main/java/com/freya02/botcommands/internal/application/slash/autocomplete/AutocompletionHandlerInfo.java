package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.slash.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.ExecutableInteractionInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandParameter;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierChoices;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringContinuity;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringFuzzy;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierTransformer;
import com.freya02.botcommands.internal.runner.MethodRunner;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// The annotated method returns a list of things
// These things can be, and are mapped as follows:
//      String -> Choice(String, String)
//      Choice -> keep the same choice
//      Object -> Transformer -> Choice
@SuppressWarnings("unchecked")
public class AutocompletionHandlerInfo implements ExecutableInteractionInfo {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;
	private final Object autocompletionHandler;
	private final Method method;
	private final MethodRunner methodRunner;

	private final String handlerName;
	private final boolean showUserInput;
	private final int maxChoices;

	private final ChoiceSupplier choiceSupplier;

	private final MethodParameters<SlashCommandParameter> autocompleteParameters;

	public AutocompletionHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.context = context;
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.methodRunner = context.getMethodRunnerFactory().make(autocompletionHandler, method);

		final AutocompletionHandler annotation = method.getAnnotation(AutocompletionHandler.class);
		final AutocompletionMode autocompletionMode = annotation.mode();
		this.handlerName = annotation.name();
		this.showUserInput = annotation.showUserInput();
		this.maxChoices = OptionData.MAX_CHOICES - (showUserInput ? 1 : 0); //accommodate for user input

		final Class<?> collectionReturnType = ClassUtils.getCollectionReturnType(method);

		if (collectionReturnType == null) {
			throw new IllegalArgumentException("Unable to determine return type of " + Utils.formatMethodShort(method) + ", does the collection inherit Collection ?");
		}

		if (String.class.isAssignableFrom(collectionReturnType) || Long.class.isAssignableFrom(collectionReturnType) || Double.class.isAssignableFrom(collectionReturnType)) {
			this.choiceSupplier = generateSupplierFromStrings(autocompletionMode);
		} else if (Command.Choice.class.isAssignableFrom(collectionReturnType)) {
			this.choiceSupplier = new ChoiceSupplierChoices(this);
		} else {
			final AutocompletionTransformer<Object> transformer = (AutocompletionTransformer<Object>) context.getAutocompletionTransformer(collectionReturnType);

			if (transformer == null) {
				throw new IllegalArgumentException("No autocompletion transformer has been register for objects of type '" + collectionReturnType.getSimpleName() + "', for method " + Utils.formatMethodShort(method) + ", you may also check the docs for " + AutocompletionHandler.class.getSimpleName());
			}

			this.choiceSupplier = new ChoiceSupplierTransformer(this, transformer);
		}

		this.autocompleteParameters = MethodParameters.of(context, method, SlashCommandParameter::new);
	}

	Collection<?> invokeAutocompletionHandler(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event) throws IllegalAccessException, InvocationTargetException {
		List<Object> objects = new ArrayList<>(autocompleteParameters.size() + 1);

		objects.add(event);

		int optionIndex = 0;
		final List<String> optionNames = event.getGuild() != null ? slashCommand.getLocalizedOptions(event.getGuild()) : null;
		for (final SlashCommandParameter parameter : autocompleteParameters) {
			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			final Object obj;
			if (parameter.isOption()) {
				String optionName = optionNames == null ? applicationOptionData.getEffectiveName() : optionNames.get(optionIndex);
				if (optionName == null) {
					throw new IllegalArgumentException(String.format("Option name #%d (%s) could not be resolved for %s", optionIndex, applicationOptionData.getEffectiveName(), Utils.formatMethodShort(method)));
				}

				optionIndex++;

				final OptionMapping optionMapping = event.getOption(optionName);

				//Discord sends empty strings if you don't type anything
				if (optionMapping == null || optionMapping.getAsString().isEmpty()) {
					if (parameter.isPrimitive()) {
						objects.add(0);
					} else {
						objects.add(null);
					}

					continue;

					//Don't throw if option mapping is not found, this is normal under autocompletion, only some options are sent
				}

				obj = parameter.getResolver().resolve(context, slashCommand, event, optionMapping);

				if (obj == null) {
					//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
					LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return List.of();
				}

				if (!parameter.getBoxedType().isAssignableFrom(obj.getClass())) {
					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return List.of();
				}
			} else {
				obj = parameter.getCustomResolver().resolve(context, this, event);
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(obj);
		}

		return (Collection<?>) method.invoke(autocompletionHandler, objects.toArray());
	}

	private ChoiceSupplier generateSupplierFromStrings(AutocompletionMode autocompletionMode) {
		if (autocompletionMode == AutocompletionMode.FUZZY) {
			return new ChoiceSupplierStringFuzzy(this);
		} else {
			return new ChoiceSupplierStringContinuity(this);
		}
	}

	public static Command.Choice getChoice(OptionMapping optionMapping, String string) {
		return switch (optionMapping.getType()) {
			case STRING -> new Command.Choice(string, string);
			case INTEGER -> new Command.Choice(string, Long.parseLong(string));
			case NUMBER -> new Command.Choice(string, Double.parseDouble(string));
			default -> throw new IllegalArgumentException("Invalid autocompletion option type: " + optionMapping.getType());
		};
	}

	public String getHandlerName() {
		return handlerName;
	}

	public List<Command.Choice> getChoices(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event) throws Exception {
		final List<Command.Choice> actualChoices = new ArrayList<>(25);

		final List<Command.Choice> suppliedChoices = choiceSupplier.apply(slashCommand, event, invokeAutocompletionHandler(slashCommand, event));

		final OptionMapping optionMapping = event.getFocusedOption();

		//If something is typed but there are no choices, don't display user input
		if (showUserInput && !optionMapping.getAsString().isBlank() && !suppliedChoices.isEmpty())
			actualChoices.add(getChoice(optionMapping, optionMapping.getAsString()));

		for (int i = 0; i < maxChoices && i < suppliedChoices.size(); i++) {
			actualChoices.add(suppliedChoices.get(i));
		}

		return actualChoices;
	}

	@Override
	@NotNull
	public Method getMethod() {
		return method;
	}

	@Override
	@NotNull
	public MethodRunner getMethodRunner() {
		return methodRunner;
	}

	@Override
	@NotNull
	public MethodParameters<? extends CommandParameter<?>> getParameters() {
		return autocompleteParameters;
	}

	@Override
	@NotNull
	public Object getInstance() {
		return autocompletionHandler;
	}

	public void checkParameters(SlashCommandInfo info) {
		final List<? extends SlashCommandParameter> slashOptions = info.getOptionParameters();

		autocompleteParameterLoop:
		for (SlashCommandParameter autocompleteParameter : autocompleteParameters) {
			if (!autocompleteParameter.isOption()) continue;

			for (SlashCommandParameter slashCommandParameter : slashOptions) {
				if (slashCommandParameter.getApplicationOptionData().getEffectiveName().equals(autocompleteParameter.getApplicationOptionData().getEffectiveName())) {
					checkParameter(slashCommandParameter, autocompleteParameter);

					continue autocompleteParameterLoop;
				}
			}

			throw new IllegalArgumentException("Couldn't find parameter named %s in slash command %s".formatted(autocompleteParameter.getApplicationOptionData().getEffectiveName(), Utils.formatMethodShort(info.getMethod())));
		}
	}

	private void checkParameter(SlashCommandParameter slashCommandParameter, SlashCommandParameter autocompleteParameter) {
		if (!slashCommandParameter.isOption()) return;

		final Class<?> slashParameterType = slashCommandParameter.getBoxedType();
		final Class<?> autocompleteParameterType = autocompleteParameter.getBoxedType();
		if (!slashParameterType.equals(autocompleteParameterType)) {
			throw new IllegalArgumentException("Autocompletion handler parameter #%d does not have the same type as slash command parameter: Provided: %s, correct: %s".formatted(autocompleteParameter.getIndex(), autocompleteParameterType, slashParameterType));
		}
	}

	public int getMaxChoices() {
		return maxChoices;
	}
}
