package bytecodeblinder.user;

import java.util.ArrayList;

public class User {
    private String username;
    private String fullname;
    private String address;
    private String email;
    private String dob;
    private String gender;
    private String password;
    private String createDate;
    private boolean isOnline;

    private ArrayList<User> friends;

    private ArrayList<ChatMessage> messages;
    private MessageListener listener;

    @Override
    public String toString() {

        return this.username;
    }

    void setPassword(String password) {
        this.password = password;
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
        this.friends = null;
    };

    public User(String username,
            String fullname) {
        this.username = username;
        this.fullname = fullname;
    };

    public User(String username) {
        this.username = username;

    };

    interface MessageListener {
        void onNewMessage();
    }

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

    public String getPassword() {
        return password;
    }

    public ArrayList<User> getFriends() {

        return friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    public void setMessages(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        listener.onNewMessage();
    }

    void addMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    void removeMessageListener() {
        this.listener = null;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    boolean isOnline() {
        return this.isOnline;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return username != null && username.equals(user.username);
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreateDate() {
        return this.createDate;
    }

}
