# Template bot

This is a bot that you can take as an example;
it has a `/ban` command which accepts a User, a timeframe of messages to be deleted and a reason for the ban.
This also contains a text command `exit` to shut down the bot.

## Additional requirements

* A PostgreSQL database
* Your bot token

## Configuration
### Dev mode
You need to copy the `config-template` folder's content in `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

No need to keep the `logback.xml`, only `logback-test.xml` is necessary.

[//]: # (https://tree.nathanfriend.io/?s=%28%27options%21%28%27fancy%21true%7EfullPath%21false%7EtrailingSlash%21true%7ErootDot%21false%29%7E*%28%27*%27%27%29%7Eversion%21%271%27%29*source%21%01*)

Your file tree should look like this:
```
IntelliJ-Projects/
└── BotTemplate/
    ├── dev-config/
    │   ├── config.json
    │   └── logback-test.xml
    ├── src/
    │   └── ..
    └── pom.xml
```

You can then just run the `Main` class.

### Prod mode
You must make the jar using `mvn package`, then, create a folder and put the JAR in it.<br>
You can then copy the same `config-template` folder's content in `config`, edit the `config.json`.

No need to keep the `logback-test.xml`, only `logback.xml` is necessary.

Your file tree should look like this:
```
BotTemplate/
├── config/
│   ├── config.json
│   └── logback.xml
└── BotTemplate.jar
```

You can then run your bot using `java -jar BotTemplate.jar`, for example.