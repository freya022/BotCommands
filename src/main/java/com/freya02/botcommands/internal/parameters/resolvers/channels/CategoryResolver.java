package com.freya02.botcommands.internal.parameters.resolvers.channels;

import com.freya02.botcommands.internal.annotations.IncludeClasspath;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

@IncludeClasspath
public class CategoryResolver extends AbstractChannelResolver<Category> {
	public CategoryResolver() {
		super(Category.class, ChannelType.CATEGORY, Guild::getCategoryById);
	}
}
