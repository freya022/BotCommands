package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CompositeKey;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import com.freya02.botcommands.internal.application.slash.ApplicationCommandVarArgParameter;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.lang.reflect.Parameter;

public class AutocompleteCommandParameter extends ApplicationCommandVarArgParameter<SlashParameterResolver> {
	private final boolean compositeKey;

	public AutocompleteCommandParameter(Parameter parameter, int index) {
		super(SlashParameterResolver.class, parameter, index);

		if (User.class.isAssignableFrom(getBoxedType())
				|| Member.class.isAssignableFrom(getBoxedType())
				|| Channel.class.isAssignableFrom(getBoxedType())
				|| Role.class.isAssignableFrom(getBoxedType())) {
			throw new IllegalArgumentException("Autocomplete parameters cannot have an entity (User/Member/Channel/Role) as a value");
		}

		this.compositeKey = parameter.isAnnotationPresent(CompositeKey.class);
	}

	public boolean isCompositeKey() {
		return compositeKey;
	}
}
