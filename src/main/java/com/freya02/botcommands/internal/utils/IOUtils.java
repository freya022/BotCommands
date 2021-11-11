package com.freya02.botcommands.internal.utils;

import java.nio.file.Path;

public final class IOUtils {

	public static String getFileExtension(Path path) {
		final String pathStr = path.toString();
		return pathStr.substring(pathStr.lastIndexOf('.') + 1);
	}
}
