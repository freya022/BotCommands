package com.freya02.botcommands.api.modals;

import java.util.concurrent.TimeUnit;

public record ModalTimeoutInfo(long timeout, TimeUnit unit, Runnable onTimeout) {}
