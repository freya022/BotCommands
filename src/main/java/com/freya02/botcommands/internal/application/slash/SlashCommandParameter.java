package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.EnumSet;

public class SlashCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	private final Number minValue, maxValue;
	private final EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
	private final TLongObjectMap<DefaultValueSupplier> defaultOptionSupplierMap = new TLongObjectHashMap<>();

	public SlashCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);

		if (parameter.isAnnotationPresent(TextOption.class))
			throw new IllegalArgumentException(String.format("Slash command parameter #%d of %s#%s cannot be annotated with @TextOption", index, parameter.getDeclaringExecutable().getDeclaringClass().getName(), parameter.getDeclaringExecutable().getName()));

		final LongRange longRange = ReflectionUtils.getLongRange(parameter);
		if (longRange != null) {
			minValue = longRange.from();
			maxValue = longRange.to();
		} else {
			final DoubleRange doubleRange = ReflectionUtils.getDoubleRange(parameter);
			if (doubleRange != null) {
				minValue = doubleRange.from();
				maxValue = doubleRange.to();
			} else {
				minValue = OptionData.MIN_NEGATIVE_NUMBER;
				maxValue = OptionData.MAX_POSITIVE_NUMBER;
			}
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

	public TLongObjectMap<DefaultValueSupplier> getDefaultOptionSupplierMap() {
		return defaultOptionSupplierMap;
	}
}
