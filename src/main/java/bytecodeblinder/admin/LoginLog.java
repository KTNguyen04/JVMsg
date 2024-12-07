package bytecodeblinder.admin;

public class LoginLog {
    private String username;
    private String loginTime;
    private String fullname;

    public LoginLog(String username, String fullname, String loginTime) {
        this.username = username;
        this.loginTime = loginTime;
        this.fullname = fullname;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public String getUsername() {
        return username;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }
}
