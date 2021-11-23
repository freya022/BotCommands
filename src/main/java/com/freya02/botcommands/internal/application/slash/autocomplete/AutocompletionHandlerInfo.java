package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.application.slash.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandParameter;
import com.freya02.botcommands.internal.utils.Utils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// The annotated method returns a list of things
// These things can be, and are mapped as follows:
//      String -> Choice(String, String)
//      Choice -> keep the same choice
//      Object -> Transformer -> Choice
@SuppressWarnings("unchecked")
public class AutocompletionHandlerInfo {
	private static final Logger LOGGER = Logging.getLogger();
	private static final int MAX_CHOICES = OptionData.MAX_CHOICES - 1; //accommodate for user input

	private final Object autocompletionHandler;
	private final Method method;

	private final String handlerName;

	private final ChoiceSupplier choiceSupplier;
	private final AutocompletionTransformer<Object> transformer;

	private final MethodParameters<SlashCommandParameter> autocompleteParameters;

	public AutocompletionHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;

		final AutocompletionHandler annotation = method.getAnnotation(AutocompletionHandler.class);
		final AutocompletionMode autocompletionMode = annotation.mode();
		this.handlerName = annotation.name();

		Class<?> collectionReturnType = ClassUtils.getCollectionReturnType(method);
		this.transformer = (AutocompletionTransformer<Object>) context.getAutocompletionTransformer(collectionReturnType);

		if (collectionReturnType == null) {
			throw new IllegalArgumentException("Unable to determine return type of " + Utils.formatMethodShort(method) + ", is the collection a List ?");
		}

		if (String.class.isAssignableFrom(collectionReturnType) || Long.class.isAssignableFrom(collectionReturnType) || Double.class.isAssignableFrom(collectionReturnType)) {
			this.choiceSupplier = generateSupplierFromStrings(autocompletionMode);
		} else if (SlashCommand.Choice.class.isAssignableFrom(collectionReturnType)) {
			this.choiceSupplier = generateSupplierFromChoices();
		} else {
			if (context.getAutocompletionTransformer(collectionReturnType) == null) {
				throw new IllegalArgumentException("No autocompletion transformer has been register for objects of type '" + collectionReturnType.getSimpleName() + "', for method " + Utils.formatMethodShort(method) + ", you may also check the docs for " + AutocompletionHandler.class.getSimpleName());
			}

			this.choiceSupplier = generateSupplierFromItems();
		}

		this.autocompleteParameters = MethodParameters.of(method, SlashCommandParameter::new);
	}

	private Object invokeAutocompletionHandler(SlashCommandInfo slashCommand, CommandAutoCompleteEvent event) throws IllegalAccessException, InvocationTargetException {
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

				if (optionMapping == null) {
					if (parameter.isPrimitive()) {
						objects.add(0);
					} else {
						objects.add(null);
					}

					continue;

					//Don't throw if option mapping is not found, this is normal under autocompletion, only some options are sent
				}

				obj = parameter.getResolver().resolve(event, optionMapping);

				if (obj == null) {
					//Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
					LOGGER.trace("The parameter '{}' of value '{}' could not be resolved into a {}", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}

				if (!parameter.getBoxedType().isAssignableFrom(obj.getClass())) {
					LOGGER.error("The parameter '{}' of value '{}' is not a valid type (expected a {})", applicationOptionData.getEffectiveName(), optionMapping.getAsString(), parameter.getBoxedType().getSimpleName());

					return false;
				}
			} else {
				obj = parameter.getCustomResolver().resolve(event);
			}

			//For some reason using an array list instead of a regular array
			// magically unboxes primitives when passed to Method#invoke
			objects.add(obj);
		}

		return method.invoke(autocompletionHandler, objects.toArray());
	}

	private ChoiceSupplier generateSupplierFromChoices() {
		return (slashCommand, event) -> {
			final List<SlashCommand.Choice> choices = (List<SlashCommand.Choice>) invokeAutocompletionHandler(slashCommand, event);

			return choices.subList(0, Math.min(MAX_CHOICES, choices.size()));
		};
	}

	private ChoiceSupplier generateSupplierFromItems() {
		return (slashCommand, event) -> {
			final List<Object> results = (List<Object>) invokeAutocompletionHandler(slashCommand, event);

			return results.stream()
					.limit(MAX_CHOICES)
					.map(transformer::apply)
					.collect(Collectors.toList());
		};
	}

	private ChoiceSupplier generateSupplierFromStrings(AutocompletionMode autocompletionMode) {
		if (autocompletionMode == AutocompletionMode.FUZZY) {
			return generateFuzzySupplier();
		} else {
			return generateContinuitySupplier();
		}
	}

	private ChoiceSupplier generateContinuitySupplier() {
		return (slashCommand, event) -> {
			final OptionMapping optionMapping = event.getFocusedOptionType();

			final String query = optionMapping.getAsString();
			final List<String> list = ((List<Object>) invokeAutocompletionHandler(slashCommand, event))
					.stream()
					.map(Object::toString)
					.filter(s -> s.startsWith(query))
					.sorted()
					.collect(Collectors.toCollection(ArrayList::new));

			final List<ExtractedResult> results = FuzzySearch.extractTop(query,
					list,
					FuzzySearch::ratio,
					MAX_CHOICES);

			return results.stream()
					.limit(MAX_CHOICES)
					.map(c -> getChoice(optionMapping, c.getString()))
					.toList();
		};
	}

	@NotNull
	private ChoiceSupplier generateFuzzySupplier() {
		return (slashCommand, event) -> {
			final List<String> list = ((List<Object>) invokeAutocompletionHandler(slashCommand, event))
					.stream()
					.map(Object::toString)
					.sorted()
					.toList();

			final OptionMapping optionMapping = event.getFocusedOptionType();
			//First sort the results by similarities but by taking into account an incomplete input
			final List<ExtractedResult> bigLengthDiffResults = FuzzySearch.extractTop(optionMapping.getAsString(),
					list,
					FuzzySearch::partialRatio,
					MAX_CHOICES);

			//Then sort the results by similarities but don't take length into account
			final List<ExtractedResult> similarities = FuzzySearch.extractTop(optionMapping.getAsString(),
					bigLengthDiffResults.stream().map(ExtractedResult::getString).toList(),
					FuzzySearch::ratio,
					MAX_CHOICES);

			return similarities.stream()
					.limit(MAX_CHOICES)
					.map(c -> getChoice(optionMapping, c.getString()))
					.toList();
		};
	}

	private SlashCommand.Choice getChoice(OptionMapping optionMapping, String string) {
		return switch (optionMapping.getType()) {
			case STRING -> new SlashCommand.Choice(string, string);
			case INTEGER -> new SlashCommand.Choice(string, Long.parseLong(string));
			case NUMBER -> new SlashCommand.Choice(string, Double.parseDouble(string));
			default -> throw new IllegalArgumentException("Invalid autocompletion option type: " + optionMapping.getType());
		};
	}

	public String getHandlerName() {
		return handlerName;
	}

	public List<SlashCommand.Choice> getChoices(SlashCommandInfo slashCommand, CommandAutoCompleteEvent event) throws Exception {
		final List<SlashCommand.Choice> actualChoices = new ArrayList<>(25);

		final List<SlashCommand.Choice> suppliedChoices = choiceSupplier.apply(slashCommand, event);

		final OptionMapping optionMapping = event.getFocusedOptionType();
		if (!optionMapping.getAsString().isBlank())
			actualChoices.add(getChoice(optionMapping, optionMapping.getAsString()));

		for (int i = 0; i < MAX_CHOICES && i < suppliedChoices.size(); i++) {
			actualChoices.add(suppliedChoices.get(i));
		}

		return actualChoices;
	}

	public Method getMethod() {
		return method;
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

			throw new IllegalArgumentException("Couldn't find parameter named %s in slash command %s".formatted(autocompleteParameter.getApplicationOptionData().getEffectiveName(), Utils.formatMethodShort(info.getCommandMethod())));
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
}
