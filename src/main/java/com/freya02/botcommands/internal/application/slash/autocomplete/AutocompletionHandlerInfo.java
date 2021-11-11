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

			return choices.subList(0, Math.min(OptionData.MAX_CHOICES, choices.size()));
		};
	}

	private ChoiceSupplier generateSupplierFromItems() {
		return event -> {
			final List<Object> results = (List<Object>) method.invoke(autocompletionHandler, event);

			return results.stream()
					.limit(OptionData.MAX_CHOICES)
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
			final List<String> list = ((List<Object>) method.invoke(autocompletionHandler, event))
					.stream()
					.map(Object::toString)
					.collect(Collectors.toCollection(ArrayList::new));

			final OptionMapping optionMapping = event.getFocusedOptionType();

			list.removeIf(s -> !s.startsWith(optionMapping.getAsString()));

			final List<ExtractedResult> results = FuzzySearch.extractTop(optionMapping.getAsString(),
					list,
					FuzzySearch::ratio,
					25);

			return results.stream()
					.limit(OptionData.MAX_CHOICES)
					.map(c -> getChoice(optionMapping, c))
					.toList();
		};
	}

	@NotNull
	private ChoiceSupplier generateFuzzySupplier() {
		return event -> {
			final List<String> list = ((List<Object>) method.invoke(autocompletionHandler, event))
					.stream()
					.map(Object::toString)
					.toList();

			final OptionMapping optionMapping = event.getFocusedOptionType();
			final List<ExtractedResult> results = FuzzySearch.extractTop(optionMapping.getAsString(),
					list,
					FuzzySearch::partialRatio,
					25);

			return results.stream()
					.limit(OptionData.MAX_CHOICES)
					.map(c -> getChoice(optionMapping, c))
					.toList();
		};
	}

	private SlashCommand.Choice getChoice(OptionMapping optionMapping, ExtractedResult result) {
		return switch (optionMapping.getType()) {
			case STRING -> new SlashCommand.Choice(result.getString(), result.getString());
			case INTEGER -> new SlashCommand.Choice(result.getString(), Long.parseLong(result.getString()));
			case NUMBER -> new SlashCommand.Choice(result.getString(), Double.parseDouble(result.getString()));
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
		final List<SlashCommand.Choice> choices = choiceSupplier.apply(event);

		if (choices.size() > OptionData.MAX_CHOICES) {
			return choices.subList(0, OptionData.MAX_CHOICES);
		} else {
			return choices;
		}
	}

	public Method getMethod() {
		return method;
	}
}
