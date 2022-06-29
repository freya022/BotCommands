package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.core.api.annotations.BService;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;

@BService
public class CategoryResolver extends AbstractChannelResolver<Category> {
	public CategoryResolver() {
		super(ParameterType.ofClass(Category.class), ChannelType.CATEGORY, Guild::getCategoryById);
	}
}
