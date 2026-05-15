package OOP2ProjectFinal;

import java.util.Objects;

abstract class User {
    private final String userId;
    private String name;
    private String username;
    private String password;

    protected User(String userId, String name, String username, String password) {
        this.userId = Objects.requireNonNull(userId);
        this.name = Objects.requireNonNull(name);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public String userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public String username() {
        return username;
    }

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username);
    }

    public void setPassword(String password) {
        this.password = Objects.requireNonNull(password);
    }

    public abstract String role();
}