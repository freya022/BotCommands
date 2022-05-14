package com.freya02.botcommands.internal.parameters.channels;

import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;

public class CategoryResolver extends AbstractChannelResolver<Category> {
	public CategoryResolver() {
		super(ParameterType.ofClass(Category.class), ChannelType.CATEGORY, Guild::getCategoryById);
	}
}
