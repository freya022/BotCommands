package io.github.freya022.botcommands.api.pagination.menu;

import java.util.List;

public record MenuPage<E>(String content, List<E> entries) {}