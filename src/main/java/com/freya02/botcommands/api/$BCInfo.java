package com.freya02.botcommands.api;

import java.time.Instant;

public class $BCInfo {
	public static final Instant BUILD_TIME;
	public static final String VERSION_MAJOR = "%%version-major%%";
	public static final String VERSION_MINOR = "%%version-minor%%";
	public static final String VERSION_REVISION = "%%version-revision%%";
	public static final String VERSION_CLASSIFIER = "%%version-classifier%%";
	public static final String GITHUB = "https://github.com/freya022/BotCommands";
	/** May be "null", may also be a full commit hash in Jitpack builds */
	public static final String BRANCH_NAME = "%%branch-name%%";
	/** May be "null" */
	public static final String COMMIT_HASH = "%%commit-hash%%";
	public static final String BUILD_JDA_VERSION = "%%build-jda-version%%";

	@SuppressWarnings("ConstantConditions")
	public static final String VERSION = "%s.%s.%s%s%s".formatted(VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION,
			VERSION_CLASSIFIER == null ? "" : "-" + VERSION_CLASSIFIER,
			COMMIT_HASH.equals("null") ? "" : "_" + COMMIT_HASH);

	static {
		Instant tmpBuildTime;
		try {
			tmpBuildTime = Instant.ofEpochMilli(Long.parseLong("%%build-time%%"));
		} catch (NumberFormatException e) { //Can happen on IJ builds, ig
			tmpBuildTime = Instant.now();
		}
		BUILD_TIME = tmpBuildTime;
	}
}
