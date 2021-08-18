package com.freya02.botcommands;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class IOUtils {
	static Path getJarPath(Class<?> classs) throws IOException {
		try {
			return Paths.get(classs.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			throw new IOException("Could not get the location of " + classs, e);
		}
	}

	static String getFileExtension(Path path) {
		final String pathStr = path.toString();
		return pathStr.substring(pathStr.lastIndexOf('.') + 1);
	}
}
