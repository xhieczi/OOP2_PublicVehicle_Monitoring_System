package OOP2Project;

class Operator extends User {
    public Operator(String userId, String name, String username, String password) {
        super(userId, name, username, password);
    }
    @Override public String role() { return "OPERATOR"; }
}