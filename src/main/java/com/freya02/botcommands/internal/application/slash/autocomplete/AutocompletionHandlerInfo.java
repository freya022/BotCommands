package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.api.application.slash.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionMode;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.Utils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

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
	private static final int MAX_CHOICES = OptionData.MAX_CHOICES - 1; //accommodate for user input

	private final Object autocompletionHandler;
	private final Method method;

	private final Class<?> collectionReturnType;

	private final String handlerName;

	private final ChoiceSupplier choiceSupplier;
	private final AutocompletionTransformer<Object> transformer;

	public AutocompletionHandlerInfo(BContextImpl context, Object autocompletionHandler, Method method) {
		this.autocompletionHandler = autocompletionHandler;
		this.method = method;
		this.collectionReturnType = ClassUtils.getCollectionReturnType(method);

		final AutocompletionHandler annotation = method.getAnnotation(AutocompletionHandler.class);
		final AutocompletionMode autocompletionMode = annotation.mode();
		this.handlerName = annotation.name();

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
	}

	private ChoiceSupplier generateSupplierFromChoices() {
		return event -> {
			final List<SlashCommand.Choice> choices = (List<SlashCommand.Choice>) method.invoke(autocompletionHandler, event);

			return choices.subList(0, Math.min(MAX_CHOICES, choices.size()));
		};
	}

	private ChoiceSupplier generateSupplierFromItems() {
		return event -> {
			final List<Object> results = (List<Object>) method.invoke(autocompletionHandler, event);

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
		return event -> {
			final OptionMapping optionMapping = event.getFocusedOptionType();

			final String query = optionMapping.getAsString();
			final List<String> list = ((List<Object>) method.invoke(autocompletionHandler, event))
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
		return event -> {
			final List<String> list = ((List<Object>) method.invoke(autocompletionHandler, event))
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

	public Class<?> getCollectionReturnType() {
		return collectionReturnType;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public List<SlashCommand.Choice> getChoices(CommandAutoCompleteEvent event) throws Exception {
		final List<SlashCommand.Choice> actualChoices = new ArrayList<>(25);

		final List<SlashCommand.Choice> suppliedChoices = choiceSupplier.apply(event);

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
}
