package com.freya02.botcommands.slash;

/**
 * Every slash command has to inherit this class
 * <br>You are able to get a BContext by putting it in your constructor, this will work because of constructor injection.
 */
public abstract class SlashCommand implements GuildSlashSettings {}