package cz.cvut.user.util;

public class UserWrapper {
    private String username;
    private boolean isAdmin;

    public UserWrapper() {
    }

    public UserWrapper(String username, boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
