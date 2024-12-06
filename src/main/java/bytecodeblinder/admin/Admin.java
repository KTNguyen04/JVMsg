package bytecodeblinder.admin;

import java.util.ArrayList;

import bytecodeblinder.user.User;

class Admin {
    private ArrayList<User> users;

    void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    ArrayList<User> getUsers() {
        return this.users;
    }
}
