package server;

import user.User;
import user.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;

class DatabaseController {

    private String hostName = "localhost:3306";
    private String USERS = "USERS";
    private String username = "JVMsg";
    private String password = "JVMsg741*";
    private String connectionURL = "jdbc:mysql://" + hostName + "/" + USERS;

    DatabaseController() {
    }

    private Connection connect() {
        Connection connect = null;
        try {
            connect = DriverManager.getConnection(connectionURL, username, password);
            System.out.println("Connect to database successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connect;
    }

    String getUserData(String username) {
        Connection connection = connect();

        String query = String.format("SELECT username,fullname,address,gender,dob,email from %s.%s where username= ?",
                this.USERS, "USER");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            rs = pstm.executeQuery();

            User user = null;
            if (rs.next()) {

                String usrn = rs.getString("username");
                String fullname = rs.getString("fullname");
                String address = rs.getString("address");
                String email = rs.getString("email");
                String dob = rs.getString("dob");
                String gender = rs.getString("gender");

                user = new User(usrn, fullname, address, email, dob, gender);

            }
            connection.close();

            user.setFriends(getUserFriends(username));
            // user.setMessages(getMessage(username, username));
            Gson gson = new Gson();
            return gson.toJson(user);
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return "";
    }

    boolean insertMessage(Message msg) {
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s " +
                "VALUES (?, ?,?,?);",
                this.USERS, "MESSAGE");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> friends = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, msg.getFrom());
            pstm.setString(2, msg.getTo());
            pstm.setString(3, msg.getTimeStamp());
            System.out.println("ts" + msg.getTimeStamp());
            pstm.setString(4, msg.getContent());
            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

                return true;
            }
            connection.close();
            // System.out.println("123" + friends.get(0));

            // return true;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return false;
    }

    ArrayList<User> getUserFriends(String username) {
        Connection connection = connect();

        String query = String.format("\n" + //
                "        SELECT " + //
                "        u.username,u.fullname,u.address,u.gender,u.dob,u.email\n" + //
                "    FROM \n" + //
                "        %s.%s f\n" + //
                "    JOIN \n" + //
                "       %s.%s u\n" + //
                "        ON u.username = \n" + //
                "            CASE \n" + //
                "                WHEN f.username1 = ? THEN f.username2 \n" + //
                "                ELSE f.username1 \n" + //
                "            END\n" + //
                "    WHERE \n" + //
                "        f.username1 = ? OR f.username2 = ?;",
                this.USERS, "FRIENDS", this.USERS, "USER");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> friends = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            pstm.setString(2, username);
            pstm.setString(3, username);
            rs = pstm.executeQuery();
            while (rs.next()) {

                String usrn = rs.getString("username");
                String fullname = rs.getString("fullname");
                String address = rs.getString("address");
                String email = rs.getString("email");
                String dob = rs.getString("dob");
                String gender = rs.getString("gender");

                User user = new User(usrn, fullname, address, email, dob, gender);
                friends.add(user);

            }
            connection.close();
            // System.out.println("123" + friends.get(0));

            return friends;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return friends;
    }

    String getUserMessage(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("select * from %s.%s m " +
                "where (m.from = ? and m.to = ? )or (m.from = ? and m.to = ?) " +
                "ORDER BY m.time_stamp;",
                this.USERS, "MESSAGE");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<Message> messages = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);
            rs = pstm.executeQuery();
            while (rs.next()) {

                String from = rs.getString("from");
                String to = rs.getString("to");
                String timeStamp = rs.getString("time_stamp");
                String content = rs.getString("content");

                Message msg = new Message(from, to, content, timeStamp);
                messages.add(msg);

            }
            connection.close();
            // System.out.println("123" + friends.get(0));
            Gson gson = new Gson();
            return gson.toJson(messages);
            // return messages;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return "";
    }

    boolean addOnline(String username, String ip) {
        Connection connection = connect();

        String query = String.format("insert into  %s.%s " +
                "values (?,?)", this.USERS, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

                return true;
            }
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return false;
    }

    boolean checkLogin(String username, String password) {
        Connection connection = connect();

        String query = String.format("select id from %s.%s where username= ? and password =?", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            pstm.setString(2, password);
            // stm.setString(2, password);

            rs = pstm.executeQuery();

            if (rs.next()) {
                System.out.println("THanh cong dang nhap");

                // HashMap<String, String> data = new HashMap<>();
                // data.put("header", "logined");
                // data.put("username", username);
                // data.put("fullname", fullname);
                // data.put("address", address);
                // data.put("email", email);
                // data.put("dob", dob);
                // data.put("gender", gender);
                // data.put("password", password);

                // Gson gson = new Gson();
                // String json = gson.toJson(signupData);

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        return false;

    }
}
