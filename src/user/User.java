package user;

class User {
    private String username;
    private String fullname;
    private String address;
    private String email;
    private String dob;
    private String gender;

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.username;
    }

    public User() {
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
    // public void setUsername(String username) {
    // this.username = username;
    // }

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
