package com.freya02.botcommands.api.pagination;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PaginationTimeoutConsumer<T extends BasicPagination<T>> {
	void accept(@NotNull T paginator, @Nullable Message message);
}
