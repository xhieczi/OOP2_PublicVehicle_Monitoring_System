package OOP2Project;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import OOP2Project.*;

abstract class User {
    private final String userId;
    private final String name;
    private final String username;
    private final String password; // encapsulated

    protected User(String userId, String name, String username, String password) {
        this.userId = Objects.requireNonNull(userId);
        this.name = Objects.requireNonNull(name);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public String userId() { return userId; }
    public String name() { return name; }
    public String username() { return username; }

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    public abstract String role();
}
