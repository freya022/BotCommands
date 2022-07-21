package com.freya02.botcommands.internal.modals;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class ModalHandlersBuilder {
	private static final Logger LOGGER = Logging.getLogger();

	private final BContextImpl context;

	public ModalHandlersBuilder(BContextImpl context) {
		this.context = context;

		InternalModals.setContext(context);
	}

	public void processHandler(Object autocompleteHandler, Method method) {
		try {
			if (!ReflectionUtils.hasFirstParameter(method, ModalInteractionEvent.class))
				throw new IllegalArgumentException("Modal handler at " + Utils.formatMethodShort(method) + " must have a " + ModalInteractionEvent.class.getSimpleName() + " event as first parameter");

			final ModalHandlerInfo handler = new ModalHandlerInfo(context, autocompleteHandler, method);

			context.getApplicationCommandsContext().addModalHandler(handler);

			LOGGER.debug("Adding modal handler '{}' for method {}", handler.getHandlerName(), Utils.formatMethodShort(method));
		} catch (Exception e) {
			throw new RuntimeException("An exception occurred while processing a modal handler at " + Utils.formatMethodShort(method), e);
		}
	}

	public void postProcess() {
		context.addEventListeners(new ModalListener(context));
	}
}
