package bytecodeblinder.server;

import bytecodeblinder.user.*;
import java.sql.*;
import java.util.ArrayList;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

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
            user.setIsOnline(true);
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
                this.USERS, "USER");

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
                "where (`from` = ? and `to` = ?) or (`from` = ? and `to` = ?)", this.USERS, "MESSAGE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                this.USERS, "ADD_FRIEND");

        PreparedStatement pstm = null;
        ResultSet rs = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);

            rs = pstm.executeQuery();
            while (rs.next()) {

                String usrn = rs.getString("from");
                // String fullname = rs.getString("fullname");

                User user = new User(usrn);

                users.add(user);

            }
            connection.close();
            // System.out.println("123" + friends.get(0));

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
                this.USERS, "FRIENDS");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

                return true;
            }
            connection.close();
            // System.out.println("123" + friends.get(0));
            return false;
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
                this.USERS, "BLOCK");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

                return true;
            }
            connection.close();
            // System.out.println("123" + friends.get(0));
            return false;
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
                this.USERS, "ADD_FRIEND");

        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, from);
            pstm.setString(2, to);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

                return true;
            }
            connection.close();
            // System.out.println("123" + friends.get(0));
            return false;
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
                "where (`from` = ? and `to` = ?) or (`from` = ? and `to` = ?)", this.USERS, "ADD_FRIEND");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                "where (`username1` = ? and `username2` = ?) or (`username1` = ? and `username2` = ?)", this.USERS,
                "FRIENDS");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username1);
            pstm.setString(2, username2);
            pstm.setString(3, username2);
            pstm.setString(4, username1);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
            return false;
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
                "    WHERE " + //
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
                user.setIsOnline(checkOnline(usrn));
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
                this.USERS, "MESSAGE");

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
            System.out.println("exist");
            return false;
        }
        Connection connection = connect();

        String query = String.format("INSERT INTO %s.%s (username, password, fullname, address, gender, dob,email) " +
                "VALUES (?, ?, ?, ?, ?, ?,?);", this.USERS, "USER");
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
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                System.out.println("CHECK");
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
                "where username=?;", this.USERS, "USER");
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

            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                "where username=?;", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            pstm.setString(1, hashedPassword);
            pstm.setString(2, username);

            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                "where username =?", this.USERS, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            rs = pstm.executeQuery();
            // rs = pstm.executeQuery();

            if (rs.next()) {
                System.out.println("THanh cong");
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
                "where username=?", this.USERS, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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
                "values (?)", this.USERS, "ONLINE");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, ip);
            // stm.setString(2, password);

            int row = pstm.executeUpdate();
            // rs = pstm.executeQuery();

            if (row != 0) {
                System.out.println("THanh cong");

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

        String query = String.format("select password from %s.%s where username= ?", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, password);
            // stm.setString(2, password);

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

        String query = String.format("select username from %s.%s where username= ?", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, password);
            // stm.setString(2, password);

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

        String query = String.format("select email from %s.%s where username= ?", this.USERS, "USER");
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = connection.prepareStatement(query);
            pstm.setString(1, username);
            // pstm.setString(2, password);
            // stm.setString(2, password);

            rs = pstm.executeQuery();

            if (rs.next()) {
                System.out.println("has usrn");
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
                this.USERS, "USER");

        // PreparedStatement pstm = null;
        Statement stm = null;
        ResultSet rs = null;
        ArrayList<User> requests = new ArrayList<>();
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

                requests.add(user);

            }
            connection.close();
            // System.out.println("123" + friends.get(0));

            return requests;
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
        return requests;

    }
}
