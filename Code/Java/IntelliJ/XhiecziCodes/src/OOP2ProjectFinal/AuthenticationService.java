package OOP2ProjectFinal;

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

    public boolean updateUserProfile(User user, String newName, String newUsername) {
        if (user == null) return false;

        String oldUsername = user.username();

        if (!oldUsername.equalsIgnoreCase(newUsername) && usersByUsername.containsKey(newUsername)) {
            return false;
        }

        usersByUsername.remove(oldUsername);
        user.setName(newName);
        user.setUsername(newUsername);
        usersByUsername.put(newUsername, user);

        return true;
    }

    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (user == null) return false;

        if (!user.checkPassword(currentPassword)) {
            return false;
        }

        user.setPassword(newPassword);
        return true;
    }
}