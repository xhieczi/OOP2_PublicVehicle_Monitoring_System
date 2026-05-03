package OOP2Project;

import java.util.HashMap;
import java.util.Map;

class AuthenticationService {
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersById = new HashMap<>();
    private User currentUser;

    public void addUser(User u) {
        usersByUsername.put(u.username(), u);
        usersById.put(u.userId(), u);
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

    public boolean userIdExists(String userId) {
        return usersById.containsKey(userId);
    }
}
