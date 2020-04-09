package com.freya02.botcommands;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class IOUtils {
	static Path getJarPath(Class<?> classs) {
		try {
			return Paths.get(classs.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	static String getFileExtension(Path path) {
		return path.toString().substring(path.toString().lastIndexOf(".")+1);
	}
}
