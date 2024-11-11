package server;

import user.User;
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
