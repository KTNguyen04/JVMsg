package server;

import java.sql.*;

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

        String query = String.format("select username,fullname,address,gender,dob,email from %s.%s where username= ?",
                this.USERS, "USER");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            rs = pstm.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (rs.next()) {
                HashMap<String, String> data = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    data.put(columnName, rs.getString(i));

                    String columnValue = rs.getString(i);
                    System.out.println(columnName + ": " + columnValue);
                }

                Gson gson = new Gson();
                return gson.toJson(data);
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
        return "false";
    }

    boolean checkLogin(String username, String password) {
        Connection connection = connect();

        String query = String.format("select id from %s.%s where username= ?", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
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
