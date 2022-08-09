package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.annotations.VarArgs;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.CacheAutocompletion;
import com.freya02.botcommands.internal.*;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandParameter;
import com.freya02.botcommands.internal.application.slash.SlashUtils;
import com.freya02.botcommands.internal.application.slash.autocomplete.caches.AbstractAutocompletionCache;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierChoices;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringContinuity;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierStringFuzzy;
import com.freya02.botcommands.internal.application.slash.autocomplete.suppliers.ChoiceSupplierTransformer;
import com.freya02.botcommands.internal.runner.MethodRunner;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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

	private final MethodParameters<AutocompleteCommandParameter> autocompleteParameters;

	private final AbstractAutocompletionCache cache;

	public AutocompletionHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.context = context;
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.methodRunner = context.getMethodRunnerFactory().make(autocompletionHandler, method);

		final AutocompletionHandler annotation = method.getAnnotation(AutocompletionHandler.class);
		final AutocompletionMode autocompletionMode = annotation.mode();

		final CacheAutocompletion cacheAutocompletion = method.getAnnotation(CacheAutocompletion.class);
		this.cache = AbstractAutocompletionCache.fromMode(this, cacheAutocompletion);

		this.handlerName = annotation.name();
		this.showUserInput = annotation.showUserInput();
		this.maxChoices = OptionData.MAX_CHOICES - (showUserInput ? 1 : 0); //accommodate for user input

		final Class<?> collectionReturnType = ReflectionUtils.getCollectionReturnType(method);

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

		this.autocompleteParameters = MethodParameters.of(context, method, AutocompleteCommandParameter::new);
	}

	public static Command.Choice getChoice(OptionType type, String string) {
		return switch (type) {
			case STRING -> new Command.Choice(string, string);
			case INTEGER -> {
				try {
					yield new Command.Choice(string, Long.parseLong(string));
				} catch (NumberFormatException e) {
					yield null;
				}
			}
			case NUMBER -> {
				try {
					yield new Command.Choice(string, Double.parseDouble(string));
				} catch (NumberFormatException e) {
					yield null;
				}
			}
			default -> throw new IllegalArgumentException("Invalid autocompletion option type: " + type);
		};
	}

	private void invokeAutocompletionHandler(SlashCommandInfo slashCommand,
	                                         CommandAutoCompleteInteractionEvent event,
	                                         Consumer<Throwable> throwableConsumer,
	                                         ConsumerEx<Collection<?>> collectionCallback) throws Exception {
		List<Object> objects = new ArrayList<>(autocompleteParameters.size() + 1);

		objects.add(event);

		for (final AutocompleteCommandParameter parameter : autocompleteParameters) {
			final Guild guild = event.getGuild();

			if (guild != null && parameter.isOption()) {
				//Resolve the target slash command parameter, so we can retrieve its default value
				final SlashCommandParameter slashParameter = slashCommand.getParameters()
						.stream()
						.filter(p -> p.getApplicationOptionData().getEffectiveName().equals(parameter.getApplicationOptionData().getEffectiveName()))
						.findAny()
						.orElseThrow(() -> new IllegalArgumentException("Could not find corresponding slash command parameter '" + parameter.getApplicationOptionData().getEffectiveName() + "' when using autocomplete"));

				final DefaultValueSupplier supplier = slashParameter.getDefaultOptionSupplierMap().get(guild.getIdLong());
				if (supplier != null) {
					final Object defaultVal = supplier.getDefaultValue(event);

					SlashUtils.checkDefaultValue(this, parameter, defaultVal);

					objects.add(defaultVal);

					continue;
				}
			}

			final int arguments = Math.max(1, parameter.getVarArgs());
			final List<Object> objectList = new ArrayList<>(arguments);

			final ApplicationOptionData applicationOptionData = parameter.getApplicationOptionData();

			for (int varArgNum = 0; varArgNum < arguments; varArgNum++) {
				if (parameter.isOption()) {
					String optionName = applicationOptionData.getEffectiveName();
					final String varArgName = SlashUtils.getVarArgName(optionName, varArgNum);

					final OptionMapping optionMapping = event.getOption(varArgName);

					// Discord sends empty strings if you don't type anything, apparently is intended behavior
					// Discord also sends invalid number strings, intended behavior too...
					if (optionMapping == null
							|| optionMapping.getAsString().isEmpty()
							|| (parameter.isPrimitive() && !optionMapping.getAsString().chars().allMatch(i -> Character.isDigit(i) || i == '.'))
					) {
						if (parameter.isPrimitive()) {
							objectList.add(0);
						} else {
							objectList.add(null);
						}

						continue;

						//Don't throw if option mapping is not found, this is normal under autocompletion, only some options are sent
					}

					final Object resolved = parameter.getResolver().resolve(context, slashCommand, event, optionMapping);

					//If this is an additional vararg then it's OK for it to be null
					if (resolved == null) {
						//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
						LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

						return;
					}

					if (!parameter.getBoxedType().isAssignableFrom(resolved.getClass())) {
						LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

						return;
					}

					objectList.add(resolved);
				} else {
					objectList.add(parameter.getCustomResolver().resolve(context, this, event));
				}
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(parameter.isVarArg() ? objectList : objectList.get(0));
		}

		methodRunner.invoke(objects.toArray(), throwableConsumer, collectionCallback);
	}

	private ChoiceSupplier generateSupplierFromStrings(AutocompletionMode autocompletionMode) {
		if (autocompletionMode == AutocompletionMode.FUZZY) {
			return new ChoiceSupplierStringFuzzy(this);
		} else {
			return new ChoiceSupplierStringContinuity(this);
		}
	}

	public String getHandlerName() {
		return handlerName;
	}

	public void retrieveChoices(SlashCommandInfo slashCommand,
	                            CommandAutoCompleteInteractionEvent event,
	                            Consumer<Throwable> throwableConsumer,
	                            Consumer<List<Command.Choice>> choiceCallback) throws Exception {
		cache.retrieveAndCall(event, choiceCallback, key -> {
			generateChoices(slashCommand, event, throwableConsumer, choices -> {
				cache.put(key, choices);

				choiceCallback.accept(choices);
			});
		});
	}

	private void generateChoices(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Consumer<Throwable> throwableConsumer, ConsumerEx<List<Command.Choice>> choiceCallback) throws Exception {
		invokeAutocompletionHandler(slashCommand, event, throwableConsumer, collection -> {
			final List<Command.Choice> actualChoices = new ArrayList<>(25);

			final List<Command.Choice> suppliedChoices = choiceSupplier.apply(event, collection);

			final AutoCompleteQuery autoCompleteQuery = event.getFocusedOption();

			//If something is typed but there are no choices, don't display user input
			if (showUserInput && !autoCompleteQuery.getValue().isBlank() && !suppliedChoices.isEmpty()) {
				final Command.Choice choice = getChoice(autoCompleteQuery.getType(), autoCompleteQuery.getValue());

				//Could be null if option mapping is malformed
				if (choice != null) {
					actualChoices.add(choice);
				}
			}

			for (int i = 0; i < maxChoices && i < suppliedChoices.size(); i++) {
				actualChoices.add(suppliedChoices.get(i));
			}

			choiceCallback.accept(actualChoices);
		});
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
	public MethodParameters<AutocompleteCommandParameter> getParameters() {
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
		for (AutocompleteCommandParameter autocompleteParameter : autocompleteParameters) {
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

	private void checkParameter(SlashCommandParameter slashCommandParameter, AutocompleteCommandParameter autocompleteParameter) {
		if (!slashCommandParameter.isOption()) return;

		final Class<?> slashParameterType = slashCommandParameter.getBoxedType();
		final Class<?> autocompleteParameterType = autocompleteParameter.getBoxedType();
		if (!slashParameterType.equals(autocompleteParameterType)) {
			throw new IllegalArgumentException("Autocompletion handler parameter #%d does not have the same type as slash command parameter: Provided: %s, correct: %s".formatted(autocompleteParameter.getIndex(), autocompleteParameterType, slashParameterType));
		}

		//If one is var arg but not the other
		if (slashCommandParameter.isVarArg() ^ autocompleteParameter.isVarArg()) {
			throw new IllegalArgumentException("Autocompletion handler parameter #%d must be annotated with @%s if the slash command option is too".formatted(autocompleteParameter.getIndex(), VarArgs.class.getSimpleName()));
		}

		if (slashCommandParameter.getVarArgs() != autocompleteParameter.getVarArgs()) {
			throw new IllegalArgumentException("Autocompletion handler parameter #%d must have the same vararg number".formatted(autocompleteParameter.getIndex()));
		}
	}

	public int getMaxChoices() {
		return maxChoices;
	}

	public void invalidate() {
		cache.invalidate();
	}
}
