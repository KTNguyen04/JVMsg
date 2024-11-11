package user;

import java.util.ArrayList;

public class User {
    private String username;
    private String fullname;
    private String address;
    private String email;
    private String dob;
    private String gender;

    ArrayList<User> friends;

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.username;
    }

    public User(String username,
            String fullname,
            String address,
            String email,
            String dob,
            String gender) {
        this.username = username;
        this.fullname = fullname;
        this.address = address;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
    };

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    // public void setFullname(String fullname) {
    // this.fullname = fullname;
    // }

    // public void setAddress(String address) {
    // this.address = address;
    // }

    // public void setEmail(String email) {
    // this.email = email;
    // }

    // public void setDob(String dob) {
    // this.dob = dob;
    // }

    // public void setGender(String gender) {
    // this.gender = gender;
    // }
}
