# Conditional instancing

Conditional instancing lets you enable classes (they may be commands, component handlers or event handlers...) with a predicate method.

The predicate is called when the class is trying to get instantiated, if the predicate returns false, the class will not be instantiated (so the constructor won't run)

## Requirements

* The method must be public and static
* It must return a boolean
* It does not accept any arguments

### Example

Suppose you have a `/windows` command that only works on Windows, and a `/linux` command that only works on Linux

```java
public class WindowsCommand extends ApplicationCommand {
	@ConditionalUse //Called when the class is about to get constructed
	public static boolean canUse() { //Return false if it's not Windows
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	@JDASlashCommand(name = "windows")
	public void execute(GuildSlashEvent event) {
		event.reply("The bot runs on Windows").setEphemeral(true).queue();
	}
}
```

```java
public class LinuxCommand extends ApplicationCommand {
	@ConditionalUse //Called when the class is about to get constructed
	public static boolean canUse() { //Return false if it's not Linux
		final String osName = System.getProperty("os.name").toLowerCase();

		return osName.contains("linux") || osName.contains("nix"); //Not accurate but should do it, not tested
	}

	@JDASlashCommand(name = "linux")
	public void execute(GuildSlashEvent event) {
		event.reply("The bot runs on Linux").setEphemeral(true).queue();
	}
}
```

This would make these commands enable themselves when the running OS corresponds