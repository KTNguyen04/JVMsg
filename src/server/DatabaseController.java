package server;

import java.sql.*;

import java.util.HashMap;

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

    String getUser(String username) {
        Connection connection = connect();

        String query = String.format("select username,fullname,address,gender,dob,email from %s.%s where username= ?",
                this.USERS, "USER");

        try (PreparedStatement pstm = connection.prepareStatement(query)) {

            pstm.setString(1, username);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                System.out.println("THanh cong dang nhap");

                String urn = rs.getString("username");
                String pass = rs.getString("password");
                String m = rs.getString("email");
                System.out.println(" - " + urn + " - " + pass + " - " + m);
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

                return "true";
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return "false";
    }

    boolean checkLogin(String username, String password) {
        Connection connection = connect();

        String query = String.format("select id from %s.%s where username= ?", this.USERS, "USER");

        try (PreparedStatement pstm = connection.prepareStatement(query)) {

            pstm.setString(1, username);
            // stm.setString(2, password);

            ResultSet rs = pstm.executeQuery();

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
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;

    }
}
