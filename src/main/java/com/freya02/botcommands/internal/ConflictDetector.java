package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.internal.utils.IOUtils;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class ConflictDetector {
	private static final Pattern PACKAGE_SPLIT_PATTERN = Pattern.compile("[/.]");
	private static final Logger LOGGER = Logging.getLogger();

	private static Path getFsPath(FileSystem fs, String packageStr) {
		final String[] split = PACKAGE_SPLIT_PATTERN.split(packageStr);

		Path path = fs.getPath("");
		for (String s : split) {
			path = path.resolve(s);
		}

		return path;
	}

	public static void detectConflicts() throws IOException {
		final String classPath = System.getProperty("java.class.path");

		for (String jarPathStr : classPath.split(";")) {
			final Path jarPath = Path.of(jarPathStr);

			final ClassPathItem classPathItem;
			if (Files.isRegularFile(jarPath) && IOUtils.getFileExtension(jarPath).equals("jar")) {
				final FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);

				//ZipFileSystem
				classPathItem = new ClassPathItem(fs, jarPath, fs.getPath(""), false);
			} else if (Files.isDirectory(jarPath)) {
				//[OS]FileSystem
				classPathItem = new ClassPathItem(jarPath.getFileSystem(), jarPath, jarPath, true);
			} else {
				continue;
			}

			try (ClassPathItem item = classPathItem) {
				final Path source = item.source;
				final Path root = item.root;
				final FileSystem fs = item.fs;

				final Map<String, Path> libMap = Map.of(
						"JDA-Utils", getFsPath(fs, "com/jagrosh/jdautilities/command"),
						"JDA-Chewtils", getFsPath(fs, "pw/chew/jdachewtils/command"),
						"Flight", getFsPath(fs, "me/devoxin/flight"),
						"jda-commands", getFsPath(fs, "com/github/kaktushose/jda/commands"),
						"rimor", getFsPath(fs, "com/jasperls/rimor")
				);

				for (Map.Entry<String, Path> entry : libMap.entrySet()) {
					final String libName = entry.getKey();
					final Path packagePath = entry.getValue();
					final Path resolved = root.resolve(packagePath);

					if (Files.exists(resolved)) {
						LOGGER.warn("Detected an incompatible command library ({}) in your classpath at path: '{}', this may interfere with this library, especially with application commands", libName, source);
					}
				}
			}
		}
	}

	private record ClassPathItem(FileSystem fs, Path source, Path root, boolean isDirectory) implements Closeable {
		@Override
		public void close() throws IOException {
			if (!isDirectory) {
				fs.close();
			}
		}
	}
}