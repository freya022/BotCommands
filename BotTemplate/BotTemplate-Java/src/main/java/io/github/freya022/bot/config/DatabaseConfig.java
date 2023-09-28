package io.github.freya022.bot.config;

public class DatabaseConfig {
    private String serverName;
    private int port;
    private String name;
    private String user;
    private String password;

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return "jdbc:postgresql://%s:%s/%s".formatted(serverName, port, name);
    }
}
