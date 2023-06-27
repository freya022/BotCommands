# Template bot

This is a bot that you can take as an example;
it has a `/ban` command which accepts a User, a timeframe of messages to be deleted and a reason for the ban.
This also contains a text command `exit` to shut down the bot.

## Additional requirements

* A PostgreSQL database
* Your bot token

## How to run it
You need to copy the `config-template` folder in `dev-config`,
and edit the `config.json`, with your bot token, prefixes, owner ID and the database details.

You can then just run the `Main` class