package com.freya02.botcommands.api.pagination.menu2;

import java.util.List;

public record MenuPage<E>(String content, List<E> entries) {}