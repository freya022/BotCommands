package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.application.slash.annotations.DoubleRange;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.ApplicationCommandParameter;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Parameter;

public class SlashCommandParameter extends ApplicationCommandParameter<SlashParameterResolver> {
	private final Number minValue, maxValue;

	public SlashCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);

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
	}

	public Number getMinValue() {
		return minValue;
	}

	public Number getMaxValue() {
		return maxValue;
	}
}
