package com.freya02.botcommands.internal.prefixed.regex;

import com.freya02.botcommands.api.prefixed.annotations.MethodOrder;
import com.freya02.botcommands.internal.Logging;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;

public class MethodComparator implements Comparator<Method> {
	private static final Logger LOGGER = Logging.getLogger();

	@Override
	public int compare(Method o1, Method o2) {
		if (o1 == o2) return 0;

		final int order1 = o1.getAnnotation(MethodOrder.class).value();
		final int order2 = o2.getAnnotation(MethodOrder.class).value();
		if (order1 != 0 && order2 != 0) {
			if (order1 == order2) {
				LOGGER.warn("Warn: Method {} and {} have the same order ({})", o1, o2, order1);
			}
			return Integer.compare(order1, order2);
		}

		final Parameter[] o2Parameters = o2.getParameters();
		final Parameter[] o1Parameters = o1.getParameters();
		for (int i = 0; i < Math.min(o1.getParameterCount(), o2.getParameterCount()); i++) {
			if (o1Parameters[i].getType() == o2Parameters[i].getType()) {
				continue;
			}

			if (o1Parameters[i].getType() == String.class) {
				return 1;
			} else {
				return -1;
			}
		}

		return 1;
	}
}