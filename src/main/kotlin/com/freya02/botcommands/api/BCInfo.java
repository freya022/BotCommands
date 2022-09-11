package com.freya02.botcommands.api;

import java.time.Instant;

public class BCInfo {
	public static final Instant BUILD_TIME;
	public static final String VERSION_MINOR = "%%version-major%%";
	public static final String VERSION_MAJOR = "%%version-minor%%";
	public static final String VERSION_REVISION = "%%version-revision%%";
	public static final String GITHUB = "https://github.com/freya022/BotCommands";
	public static final String BRANCH_NAME = "%%branch-name%%";
	public static final String COMMIT_HASH = "%%commit-hash%%";
	public static final String BUILD_JDA_VERSION = "%%build-jda-version%%";

	static {
		Instant tmpBuildTime;
		try {
			tmpBuildTime = Instant.ofEpochMilli(Integer.parseInt("%%build-time%%"));
		} catch (NumberFormatException e) { //Can happen on IJ builds, ig
			tmpBuildTime = Instant.now();
		}
		BUILD_TIME = tmpBuildTime;
	}
}
