package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;

@IncludeClasspath
public class CategoryResolver extends AbstractChannelResolver<Category> {
	public CategoryResolver() {
		super(Category.class, ChannelType.CATEGORY, Guild::getCategoryById);
	}
}
