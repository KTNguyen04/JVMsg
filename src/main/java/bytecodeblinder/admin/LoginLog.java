package bytecodeblinder.admin;

public class LoginLog {
    private String username;
    private String loginTime;

    public LoginLog(String username, String loginTime) {
        this.username = username;
        this.loginTime = loginTime;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public String getUsername() {
        return username;
    }
}
