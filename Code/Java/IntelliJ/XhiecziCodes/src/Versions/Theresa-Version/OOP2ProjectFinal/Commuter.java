package OOP2ProjectFinal;

class Commuter extends User {
    public Commuter(String userId, String name, String username, String password) {
        super(userId, name, username, password);
    }
    @Override public String role() { return "COMMUTER"; }
}
