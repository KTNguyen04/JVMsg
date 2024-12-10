package bytecodeblinder.admin;

import java.util.ArrayList;
import bytecodeblinder.user.User;

class Admin {
    private ArrayList<User> users;
    private ArrayList<LoginLog> loginLogs;

    void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    ArrayList<User> getUsers() {
        return this.users;
    }

    void setLoginLogs(ArrayList<LoginLog> loginLogs) {
        this.loginLogs = loginLogs;
    }

    ArrayList<LoginLog> getLoginLogs() {
        return loginLogs;
    }
}
