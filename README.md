<img align="right" src="assets/logo.svg" height="200" alt="BotCommands logo">

[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands?versionPrefix=3)](#getting-the-library)
[![](https://img.shields.io/badge/JDA%20Version-5.0.0--beta.11+-important)](https://github.com/discord-jda/JDA/releases)
[![image](https://discord.com/api/guilds/848502702731165738/embed.png?style=shield)](https://discord.gg/frpCcQfvTz)
[![image](https://img.shields.io/badge/Javadocs-Overview-blue)](https://freya022.github.io/BotCommands/)
[![image](https://img.shields.io/badge/Wiki-Home-blue)](https://freya022.github.io/BotCommands-Wiki/)

# BotCommands
A Kotlin-first (and Java) framework that makes creating Discord bots a piece of cake,
using the [JDA](https://github.com/discord-jda/JDA) library.

## Features

* Automatic registration of commands, resolvers, services, etc... with full dependency injection
* Text commands (annotated or manually declared), either with:
  * Automatic token parsing into your parameters
    * Suppose the prefix is `!` and the command is `ban`
      ```kt
      @JDATextCommand(name = "ban")
      fun onTextBan(event: BaseCommandEvent,
                    @TextOption user: User,
                    @TextOption timeframe: Long,
                    @TextOption unit: TimeUnit, // A resolver is used here
                    @TextOption reason: String) {
          //Ban the user
      }
      ```
    * Which can be used as: `!ban @freya02 7 days Get banned`
  * Manual token consuming
* Application commands (annotated or manually declared)
  * Slash commands with **automatic & customizable argument parsing**
    * Options can be grouped into _aggregates_, as to benefit from methods specifically on them 
    * Also supports choices, min/max values/length, channel types and autocomplete
  * Context menu commands (User / Message)
  * **Automatic smart** application commands registration
  * Entirely localizable, from the command declaration to the bot responses
* Customizable and localizable error messages
* Custom (annotated) event handlers, with priorities and async
* Modals
* _Unlimited_ data storage for components, *with persistent and ephemeral storage*
  * **They can also receive additional arguments** the same way as slash commands do
* Several utilities such as:
  * An event waiter with (multiple) preconditions, timeouts and consumers for every completion state
  * Message parsers (tokenizers, see `RichTextParser`) and emoji resolvers (turning `:joy:` into 😂)
  * Paginators and menus of different types (using components!)

While every event runs on their own coroutine,
you still need to be mindful in not blocking your bot.

## Getting Started
You are strongly recommended to have some experience with Kotlin (or Java),
OOP, [JDA](https://github.com/discord-jda/JDA) and Dependency Injection basics before you start using this library.

### Prerequisites
* An [OpenJDK 17+](https://adoptium.net/temurin/releases/?version=17) installation
* A competent IDE (I recommend IntelliJ IDEA, you can't go wrong with it in Java & Kotlin, + Live Templates)
* (Recommended) Enable method parameters names, please refer to the [wiki page](https://freya022.github.io/BotCommands-Wiki/using-commands/Inferred-option-names/)
* (Recommended) Use [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent) in development, to avoid restarting too often

## Getting the library
[![](https://img.shields.io/maven-central/v/io.github.freya022/BotCommands)](https://mvnrepository.com/artifact/io.github.freya022/BotCommands/latest)
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>io.github.freya022</groupId>
    <artifactId>BotCommands</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```

### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.freya022:BotCommands:VERSION'
}
```

Alternatively, you can use jitpack to use snapshot versions, you can refer to [the JDA wiki](https://jda.wiki/using-jda/using-new-features/) for more information

### Building / Installing manually

While I don't recommend, you can see [BUILDING.md](BUILDING.md)

## How to use
You will need to build the framework first:<br>
Kotlin:
```kt
// Create a scope for our event manager
val scope = getDefaultScope()
val manager = CoroutineEventManager(scope, 1.minutes)
manager.listener<ShutdownEvent> {
  scope.cancel()
}

BBuilder.newBuilder(manager) {
  addOwners(1234L)

  addSearchPath("io.github.freya022.bot")

  textCommands {
    usePingAsPrefix = true
  }
}
```

Java:
```java
BBuilder.newBuilder(builder -> {
    builder.addOwners(1234L);
    
    builder.addSearchPath("io.github.freya022.bot");
    
    builder.textCommands(textCommands -> {
        textCommands.setUsePingAsPrefix(true);
    });
});
```

You can then either build JDA after, or create a JDA service,
which will get started automatically, as any other service.
You can refer to the bot template ([Java](BotTemplate/BotTemplate-Java/src/main/kotlin/io/github/freya022/bot/Bot.java) / [Kotlin](BotTemplate/BotTemplate-Kotlin/src/main/kotlin/io/github/freya022/bot/Bot.kt)) for more details.

## Template bot

To get started with the framework,
you can also clone this repo and extract the `BotTemplate` folder and use it as a bot template,
of course, be sure to replace the group id as well as the artifact name, as well as providing a valid config file

## Examples

You can find a more complete bot example in the [examples](examples) folder.

## Some debugging tools

- Enable the debug/trace logs in your logback.xml file, for a logging tutorial you can look at [the wiki's logging page](https://freya022.github.io/BotCommands-Wiki/Logging)
- Look at the switches in `BDebugConfig`

[//]: # (TODO update live templates)
[//]: # (## Live templates)

[//]: # ()
[//]: # (If you use IntelliJ, you can use the "live templates" provided [in live_templates.zip]&#40;live_templates.zip&#41;, this will help you make commands and handlers with predefined templates and ask you to complete them)

[//]: # ()
[//]: # (For example: if you type `slash` in your class, this will generate a slash command declaration and ask you to complete the command name, description, etc... Of course there are many more templates, you can see all of these in `Settings > Editor > Live Templates` and in the `BotCommands` group )

[//]: # ()
[//]: # (If you don't know how to install live templates, you can follow [this guide from JetBrains]&#40;https://www.jetbrains.com/help/idea/sharing-live-templates.html#import&#41;)

## Support

Don't hesitate to join [the support server](https://discord.gg/frpCcQfvTz) if you have any question!
