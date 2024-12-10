package bytecodeblinder.server;

import bytecodeblinder.admin.LoginLog;
import bytecodeblinder.user.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.ArrayList;
import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

class DatabaseController {
    private Dotenv dotenv = Dotenv.load();
    private String host;
    private String schema;
    private String username;
    private String password;
    private String connectionURL;

    DatabaseController() {
        host = dotenv.get("db_host");
        schema = dotenv.get("db_schema");
        username = dotenv.get("db_username");
        password = dotenv.get("db_password");
        connectionURL = "jdbc:mysql://" + host + "/" + schema;
    }

    private Connection connect() {
        Connection connect = null;
        try {
            connect = DriverManager.getConnection(connectionURL, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connect;
    }

    String getUserData(String username) {
        Connection connection = connect();

        String query = String.format("SELECT username,fullname,address,gender,dob,email from %s.%s where username= ?",
                this.schema, "USER");

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
            user.setIsOnline(true);

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
                if (rs != null)

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

    ArrayList<User> findFriend(String username, String pattern) {
        Connection connection = connect();

        String query = String.format("SELECT username, fullname " +
                "FROM USERS.USER " +
                "WHERE (username LIKE ? " +
                "or fullname LIKE ?) " +
                "AND username not in " +
                "(SELECT u.username " +
                "FROM " +
                "USERS.BLOCK AS b " +
                "JOIN " +
                "USERS.USER AS u " +
                "ON " +
                "u.username = " +
                "CASE " +
                "WHEN b.username1 = ? THEN b.username2 " +
                "ELSE b.username1 " +
                "END " +
                "WHERE " +
                "b.username1 = ? " +
                "OR b.username2 = ?);  ",
                this.schema, "USER");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> friends = new ArrayList<>();
        try {
            pattern = "%" + pattern + "%";
            pstm = connection.prepareStatement(query);
            pstm.setString(1, pattern);
            pstm.setString(2, pattern);
            pstm.setString(3, username);
            pstm.setString(4, username);
            pstm.setString(5, username);

            rs = pstm.executeQuery();
            while (rs.next()) {

                String usrn = rs.getString("username");
                String fullname = rs.getString("fullname");

                User user = new User(usrn, fullname);

                friends.add(user);

            }
            connection.close();

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
                if (rs != null)

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

    boolean removeAllChat(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("delete from  %s.%s " +
                "where (`from` = ? and `to` = ?) or (`from` = ? and `to` = ?)", this.schema, "MESSAGE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    ArrayList<User> getFriendRequest(String username) {

        Connection connection = connect();

        String query = String.format("select * " +
                "from  %s.%s " +
                "where `to` = ?;",
                this.schema, "ADD_FRIEND");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();
            while (rs.next()) {

                String usrn = rs.getString("from");

                User user = new User(usrn);

                users.add(user);

            }
            connection.close();

            return users;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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
        return users;

    }

    boolean makeFriend(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s " +
                "VALUES (?, ?);",
                this.schema, "FRIENDS");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();

            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean blockFriend(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s " +
                "VALUES (?, ?);",
                this.schema, "BLOCK");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();

            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean insertAddFriend(String from, String to) {
        Connection connection = connect();

        String query = String.format("INSERT IGNORE INTO %s.%s (`from`,`to`) " +
                "VALUES (?, ?);",
                this.schema, "ADD_FRIEND");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, from);
            pstm.setString(2, to);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();

            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean removeAddFriend(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("delete from  %s.%s " +
                "where (`from` = ? and `to` = ?) or (`from` = ? and `to` = ?)", this.schema, "ADD_FRIEND");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean removeFriend(String username1, String username2) {
        Connection connection = connect();

        String query = String.format("delete from  %s.%s " +
                "where (`username1` = ? and `username2` = ?) or (`username1` = ? and `username2` = ?)", this.schema,
                "FRIENDS");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean insertMessage(ChatMessage msg) {
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s " +
                "VALUES (?, ?,?,?);",
                this.schema, "MESSAGE");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> friends = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, msg.getFrom());
            pstm.setString(2, msg.getTo());
            pstm.setString(3, msg.getTimeStamp());
            pstm.setString(4, msg.getContent());
            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

        String query = String.format("\n" +
                "        SELECT " +
                "        u.username,u.fullname,u.address,u.gender,u.dob,u.email" +
                "    FROM " +
                "        %s.%s f" +
                "    JOIN " +
                "       %s.%s u" +
                "        ON u.username = " +
                "            CASE " +
                "                WHEN f.username1 = ? THEN f.username2 " +
                "                ELSE f.username1 " +
                "            END" +
                "    WHERE " +
                "        f.username1 = ? OR f.username2 = ?;",
                this.schema, "FRIENDS", this.schema, "USER");

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
                user.setIsOnline(checkOnline(usrn));
                friends.add(user);

            }
            connection.close();

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
                if (rs != null)

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
                this.schema, "MESSAGE");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<ChatMessage> messages = new ArrayList<>();
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

                ChatMessage msg = new ChatMessage(from, to, content, timeStamp);
                messages.add(msg);

            }
            connection.close();

            Gson gson = new Gson();
            return gson.toJson(messages);

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean insertUser(User user) {

        if (checkExistAcc(user.getUsername())) {
            return false;
        }
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s (username, password, fullname, address, gender, dob,email) " +
                "VALUES (?, ?, ?, ?, ?, ?,?);", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            pstm.setString(1, user.getUsername());
            pstm.setString(2, hashedPassword);
            pstm.setString(3, user.getFullname());
            pstm.setString(4, user.getAddress());
            pstm.setString(5, user.getGender());
            pstm.setString(6, user.getDob());
            pstm.setString(7, user.getEmail());

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }

            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)
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

    boolean editUser(User user, String newPassword) {

        if (!newPassword.equals("")) {
            if (!checkLogin(user.getUsername(), user.getPassword())) {
                return false;
            }
        }

        Connection connection = connect();

        String query = String.format("update %s.%s " +
                "set password = ?, " +
                "fullname = ?, " +
                "address = ?, " +
                "gender = ?, " +
                "dob = ?, " +
                "email = ? " +
                "where username=?;", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            pstm.setString(1, hashedPassword);
            pstm.setString(2, user.getFullname());
            pstm.setString(3, user.getAddress());
            pstm.setString(4, user.getGender());
            pstm.setString(5, user.getDob());
            pstm.setString(6, user.getEmail());
            pstm.setString(7, user.getUsername());

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)
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

    boolean editPassword(String username, String password) {
        Connection connection = connect();

        String query = String.format("update %s.%s " +
                "set password = ? " +
                "where username=?;", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            pstm.setString(1, hashedPassword);
            pstm.setString(2, username);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)
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

    boolean checkOnline(String username) {
        Connection connection = connect();

        String query = String.format("select username " +
                "from  %s.%s " +
                "where username =?", this.schema, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();

            if (rs.next()) {
                return true;
            }

            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean removeOnline(String username) {
        Connection connection = connect();

        String query = String.format("delete from  %s.%s " +
                "where username=?", this.schema, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean insertOnline(String username) {
        Connection connection = connect();

        String query = String.format("insert into  %s.%s " +
                "values (?)", this.schema, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            int row = pstm.executeUpdate();

            if (row != 0) {

                return true;
            }
            connection.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

        String query = String.format("select password from %s.%s where username= ?", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();

            if (rs.next()) {
                if (BCrypt.checkpw(password, rs.getString("password")))
                    return true;
                return false;

            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean checkExistAcc(String username) {
        Connection connection = connect();

        String query = String.format("select username from %s.%s where username= ?", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();

            if (rs.next()) {
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    boolean checkReset(String username, String email) {
        Connection connection = connect();

        String query = String.format("select email from %s.%s where username= ?", this.schema, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();

            if (rs.next()) {
                if (rs.getString("email").equals(email))
                    return true;
                return false;

            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                pstm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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

    ArrayList<User> getAllUsers() {

        Connection connection = connect();
        String query = String.format("SELECT username,fullname,address,gender,dob,email,createDate from %s.%s ",
                this.schema, "USER");

        Statement stm = null;
        ResultSet rs = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            stm = connection.createStatement();

            rs = stm.executeQuery(query);
            while (rs.next()) {
                String usrn = rs.getString("username");
                String fullname = rs.getString("fullname");
                String address = rs.getString("address");
                String email = rs.getString("email");
                String dob = rs.getString("dob");
                String gender = rs.getString("gender");
                String createDate = rs.getString("createDate");

                User user = new User(usrn, fullname, address, email, dob, gender);
                user.setCreateDate(createDate);

                users.add(user);

            }
            connection.close();

            return users;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                stm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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
        return users;

    }

    ArrayList<LoginLog> getLoginData() {

        Connection connection = connect();
        String query = String.format("select l.username,u.fullname,l.time " +
                "from %s.%s l join %s.%s u " +
                "on( l.username = u.username) " +
                "order by time DESC",
                this.schema, "LOGIN", this.schema, "USER");

        Statement stm = null;
        ResultSet rs = null;
        ArrayList<LoginLog> logs = new ArrayList<>();
        try {
            stm = connection.createStatement();

            rs = stm.executeQuery(query);
            while (rs.next()) {
                String usrn = rs.getString("username");
                String loginTime = rs.getString("time");
                String fullname = rs.getString("fullname");

                LoginLog log = new LoginLog(usrn, fullname, loginTime);

                logs.add(log);

            }
            connection.close();

            return logs;
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                stm.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                if (rs != null)

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
        return logs;

    }
}
