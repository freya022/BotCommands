package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.Length;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import gnu.trove.TCollections;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.EnumSet;

public class SlashCommandParameter extends ApplicationCommandVarArgParameter<SlashParameterResolver> {
	private final Number minValue, maxValue;
	private final int minLength, maxLength;
	private final EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
	private final TLongObjectMap<DefaultValueSupplier> defaultOptionSupplierMap = TCollections.synchronizedMap(new TLongObjectHashMap<>());

	public SlashCommandParameter(BContext context, CommandPath path, Parameter parameter, int index) {
		super(context, path, SlashParameterResolver.class, parameter, index);

		if (parameter.isAnnotationPresent(TextOption.class))
			throw new IllegalArgumentException(String.format("Slash command parameter #%d of %s#%s cannot be annotated with @TextOption", index, parameter.getDeclaringExecutable().getDeclaringClass().getName(), parameter.getDeclaringExecutable().getName()));

		final LongRange longRange = ReflectionUtils.getLongRange(parameter);
		if (longRange != null) {
			if (getResolver() == null || getResolver().getOptionType() != OptionType.INTEGER) {
				throw new IllegalStateException("Cannot use @" + LongRange.class.getSimpleName() + " on a option that doesn't accept an integer");
			}

			minValue = longRange.from();
			maxValue = longRange.to();
		} else {
			final DoubleRange doubleRange = ReflectionUtils.getDoubleRange(parameter);
			if (doubleRange != null) {
				if (getResolver() == null || getResolver().getOptionType() != OptionType.NUMBER) {
					throw new IllegalStateException("Cannot use @" + DoubleRange.class.getSimpleName() + " on a option that doesn't accept a floating point number");
				}

				minValue = doubleRange.from();
				maxValue = doubleRange.to();
			} else {
				if (getBoxedType() == Integer.class) {
					minValue = Integer.MIN_VALUE;
					maxValue = Integer.MAX_VALUE;
				} else {
					minValue = OptionData.MIN_NEGATIVE_NUMBER;
					maxValue = OptionData.MAX_POSITIVE_NUMBER;
				}
			}
		}

		final Length length = parameter.getAnnotation(Length.class);
		if (length != null) {
			if (getResolver() == null || getResolver().getOptionType() != OptionType.STRING) {
				throw new IllegalStateException("Cannot use @" + Length.class.getSimpleName() + " on a option that doesn't accept a string");
			}

			minLength = length.min();
			maxLength = length.max();
		} else {
			minLength = 1;
			maxLength = OptionData.MAX_STRING_OPTION_LENGTH;
		}

		Collections.addAll(channelTypes, AnnotationUtils.getEffectiveChannelTypes(parameter));
	}

	public EnumSet<ChannelType> getChannelTypes() {
		return channelTypes;
	}

	public Number getMinValue() {
		return minValue;
	}

	public Number getMaxValue() {
		return maxValue;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public TLongObjectMap<DefaultValueSupplier> getDefaultOptionSupplierMap() {
		return defaultOptionSupplierMap;
	}
}
