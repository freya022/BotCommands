# Template bot

This is a bot that you can take as an example, it has a `/ban` command which accepts a User, a number of days of messages to be deleted and a reason for the ban.
This also contains a text command `exit` to shut down the bot

## Additional requirements

* A PostgreSQL database
* Your bot token

## How to run it
You need to copy the `ConfigTemplate.json` to `Config.json`, in the root folder of the project (where the template already is), with your bot token, prefix, owner ID and the database details

You can then just run the `Main` class