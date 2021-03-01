package com.freya02.botcommands;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

final class Utils {
	static List<Class<?>> getClasses(Path jarPath, String packageName, int maxDepth) throws IOException {
		Path walkRoot = jarPath;
		final boolean isJar = IOUtils.getFileExtension(jarPath).equals("jar");
		if (isJar) {
			final FileSystem zfs = FileSystems.newFileSystem(jarPath, null);
			walkRoot = zfs.getPath("");
		}

		if (packageName != null) {
			walkRoot = walkRoot.resolve(packageName.replace(".", "\\"));
		}

		Path finalWalkRoot = walkRoot;
		return Files.walk(walkRoot, maxDepth).filter(p -> !Files.isDirectory(p)).filter(p -> IOUtils.getFileExtension(p).equals("class")).map(p -> {
			String result;

			if (isJar) {
				result = p.toString().replace('/', '.').substring(0, p.toString().length() - 6);
			} else {
				String relativePath = p.toString().replace(finalWalkRoot.toString()+"\\","");
				result = relativePath.replace(".class", "").replace("\\", ".");

				if (packageName != null) {
					result = packageName + "." + result;
				}
			}

			try {
				return Class.forName(result, false, Utils.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found: " + result + ", is it in the class path ?");
				return null;
			}
		}).collect(Collectors.toList());
	}

	static String requireNonBlankString(String str, String onError) {
		if (str == null || str.isBlank()) {
			throw new NullPointerException(onError);
		}

		return str;
	}

	static ThreadFactory createThreadFactory(String name) {
		return runnable -> {
			final Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.setName(name);
			return thread;
		};
	}
}
