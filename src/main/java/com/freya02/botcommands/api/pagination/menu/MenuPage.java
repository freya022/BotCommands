package com.freya02.botcommands.api.pagination.menu;

import java.util.List;

public record MenuPage<E>(String content, List<E> entries) {}