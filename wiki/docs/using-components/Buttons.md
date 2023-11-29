# About buttons
## How can I use them ?
You'll first need to have some experience using [JDA's buttons](https://jda.wiki/using-jda/interactions/#buttons) directly

You will need to have your `ComponentManager` set up in order to use the Components API, which is where the buttons are created.

You can then use the `Components` class methods such as `Components#primaryButton`, which is equivalent to JDA's `Button#primary`, except it will give you a builder class where you can set the properties defined in [the Components API wiki](./The-Components-API.md)

## How to listen to button clicks

You have to make a method annotated with `#!java @JDAButtonListener` and have their first parameter be a `ButtonEvent`

Example:
```java
@JDAButtonListener(name = "test")
public void testButtonListener(ButtonEvent event) {
	event.reply("test").setEphemeral(true).queue();
}
```

## More examples

You can see more examples in the [examples directory](https://github.com/freya022/BotCommands/tree/2.X/examples/src/main/java/com/freya02/bot/componentsbot)