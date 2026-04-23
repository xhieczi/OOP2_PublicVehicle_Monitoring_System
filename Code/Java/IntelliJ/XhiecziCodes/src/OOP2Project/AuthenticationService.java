package OOP2Project;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

class AuthenticationService {
    private final Map<String, User> usersByUsername = new HashMap<>();
    private User currentUser;

    public void addUser(User u) {
        usersByUsername.put(u.username(), u);
    }

    public User login(String username, String password) {
        User u = usersByUsername.get(username);
        if (u != null && u.checkPassword(password)) {
            currentUser = u;
            return u;
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    public User currentUser() {
        return currentUser;
    }


    public boolean usernameExists(String username) {
        return usersByUsername.containsKey(username);
    }


}
