package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.internal.Logging;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public class TextCommandComparator implements Comparator<TextCommandInfo> {
	private static final Logger LOGGER = Logging.getLogger();

	@Override
	public int compare(TextCommandInfo o1, TextCommandInfo o2) {
		final Method o1CommandMethod = o1.getCommandMethod();
		final Method o2CommandMethod = o2.getCommandMethod();

		//TODO verify
		if (o1CommandMethod.getParameterTypes()[0] == BaseCommandEvent.class
				&& o2CommandMethod.getParameterTypes()[0] == CommandEvent.class) {
			return 1;
		}

		final int order1 = o1.getOrder();
		final int order2 = o2.getOrder();
		if (order1 != 0 && order2 != 0) {
			if (order1 == order2) {
				LOGGER.warn("Warn: Method {} and {} have the same order ({})", o1, o2, order1);
			}

			return Integer.compare(order1, order2);
		}

		final List<? extends TextCommandParameter> o1Parameters = o1.getOptionParameters();
		final List<? extends TextCommandParameter> o2Parameters = o2.getOptionParameters();

		for (int i = 0; i < Math.min(o1Parameters.size(), o2Parameters.size()); i++) {
			if (o1Parameters.get(i).getBoxedType() == o2Parameters.get(i).getBoxedType()) {
				continue;
			}

			if (o1Parameters.get(i).getBoxedType() == String.class) {
				return 1;
			} else {
				return -1;
			}
		}

		return 1;
	}
}
