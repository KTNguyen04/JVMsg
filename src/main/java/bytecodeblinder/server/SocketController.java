package bytecodeblinder.server;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import bytecodeblinder.admin.LoginLog;
import bytecodeblinder.user.*;

class SocketController {
    private int port;
    private int chatPort;

    private ServerSocket listenSocket;

    private ArrayList<ChatSocketThread> chatClients;

    private DatabaseModel dbc;
    private Dotenv dotenv = Dotenv.load();

    String curAccepted = null;

    SocketController() {
        this.port = Integer.parseInt(dotenv.get("serverPort"));
        this.chatPort = Integer.parseInt(dotenv.get("serverChatPort"));
        chatClients = new ArrayList<>();

        dbc = new DatabaseModel();

    }

    String getServerIP() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }

    void openChatSocket() {
        try {

            new Thread(() -> {
                try (ServerSocket lisSocket = new ServerSocket(chatPort);) {
                    while (!listenSocket.isClosed()) {

                        Socket communicateSocket = lisSocket.accept();
                        if (curAccepted != null) {
                            ChatSocketThread cst = new ChatSocketThread(communicateSocket, curAccepted);
                            cst.start();
                            chatClients.add(cst);
                            curAccepted = null;
                        }

                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    void openSocket() {
        try {

            listenSocket = new ServerSocket(port);

            new Thread(() -> {

                try {
                    do {

                        Socket communicateSocket = listenSocket.accept();

                        new ClientHandler(communicateSocket).start();

                    } while (!listenSocket.isClosed());
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    void closeSocket() {
        if (!listenSocket.isClosed()) {
            try {
                listenSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ClientHandler extends Thread {

        private Socket communicateSocket;

        ClientHandler(Socket communicateSocket) {
            this.communicateSocket = communicateSocket;
        }

        public void run() {

            try (BufferedReader br = new BufferedReader(new InputStreamReader(communicateSocket.getInputStream()));
                    PrintWriter pw = new PrintWriter(communicateSocket.getOutputStream(), true)) {

                String json = br.readLine();
                Gson gson = new Gson();
                HashMap<String, String> data = gson.fromJson(json, HashMap.class);

                switch (data.get("header")) {
                    case "signup": {
                        User usr = gson.fromJson(json, User.class);
                        String header = "";
                        if (dbc.insertUser(usr)) {
                            header = "signuped";
                        } else {
                            header = "nosignup";
                        }
                        String wrappedJson = gson.toJson(Map.of("header", header));
                        pw.println(wrappedJson);

                        break;
                    }
                    case "login": {
                        String username = data.get("username");
                        String password = data.get("password");
                        if (dbc.checkLogin(username, password)) {
                            String header = "logined";

                            String userData = dbc.getUserData(username);

                            JsonObject jsonObject = JsonParser.parseString(userData).getAsJsonObject();

                            jsonObject.addProperty("header", header);

                            String jsonResponse = gson.toJson(jsonObject);
                            pw.println(jsonResponse);

                            curAccepted = username;
                            dbc.insertOnline(username);
                        } else {
                            JsonObject jsonResponseObject = new JsonObject();
                            jsonResponseObject.addProperty("header", "nologin");
                            String jsonResponse = gson.toJson(jsonResponseObject);
                            pw.println(jsonResponse);

                        }

                        break;
                    }

                    case "messages": {
                        String messages = dbc.getUserMessage(data.get("username1"), data.get("username2"));

                        pw.println(messages);

                        break;
                    }
                    case "chat": {

                        ChatMessage msg = gson.fromJson(json, ChatMessage.class);
                        dbc.insertMessage(msg);

                        break;
                    }
                    case "edit": {

                        String header = "";
                        User usr = gson.fromJson(json, User.class);
                        if (dbc.editUser(usr, data.get("newPassword"))) {
                            header = "edited";

                        } else {
                            header = "noedit";
                        }

                        String userData = dbc.getUserData(usr.getUsername());

                        JsonObject jsonObject = JsonParser.parseString(userData).getAsJsonObject();

                        jsonObject.addProperty("header", header);

                        String jsonResponse = gson.toJson(jsonObject);

                        pw.println(jsonResponse);

                        break;
                    }
                    case "reset": {
                        String username = data.get("username");
                        String email = data.get("email");
                        String header = "";
                        String pwd = passwordUsingName(username);
                        if (dbc.checkReset(username, email)) {

                            header = "reseted";
                            String body = "<html><body>" +
                                    "<h1>Hello there!</h1>" +
                                    "<p>This is your new password</p>"
                                    +
                                    "<p>" + pwd + "</p>"
                                    +
                                    "<p>Thank you!</p>" +
                                    "</body></html>";
                            sendEmail(email,
                                    "[JVMsg] Reset Password", body);

                            dbc.editPassword(username, pwd);

                        } else {
                            header = "noreset";
                        }
                        JsonObject jsonResponseObject = new JsonObject();
                        jsonResponseObject.addProperty("header", header);
                        String jsonResponse = gson.toJson(jsonResponseObject);
                        pw.println(jsonResponse);
                        break;

                    }
                    case "offline": {
                        String username = data.get("username");
                        dbc.removeOnline(username);
                        chatClients.removeIf(client -> client.getSocketThreadName().equals(username));
                        break;
                    }
                    case "findfriend": {
                        String header = "findfriended";
                        ArrayList<User> foundFriends = dbc.findFriend(data.get("from"), data.get("username"));

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", header);
                        JsonArray friendsArray = new JsonArray();
                        for (User user : foundFriends) {
                            JsonObject friendJson = new JsonObject();
                            friendJson.addProperty("username", user.getUsername());
                            friendJson.addProperty("fullname", user.getFullname());
                            friendsArray.add(friendJson);
                        }
                        jsonObject.add("friends", friendsArray);
                        pw.println(jsonObject.toString());
                        break;
                    }
                    case "addfriend": {
                        String from = data.get("from");
                        String to = data.get("to");
                        String header = "";
                        if (dbc.insertAddFriend(from, to)) {
                            header = "addfriended";
                        } else {
                            header = "noaddfriend";
                        }
                        JsonObject jsonResponseObject = new JsonObject();
                        jsonResponseObject.addProperty("header", header);
                        String jsonResponse = gson.toJson(jsonResponseObject);
                        pw.println(jsonResponse);
                        break;
                    }
                    case "friendrequest": {
                        String header = "friendrequested";
                        ArrayList<User> requests = dbc.getFriendRequest(data.get("username"));

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", header);
                        JsonArray requestArray = new JsonArray();
                        for (User user : requests) {
                            JsonObject userJson = new JsonObject();
                            userJson.addProperty("username", user.getUsername());
                            requestArray.add(userJson);
                        }
                        jsonObject.add("requests", requestArray);
                        pw.println(jsonObject.toString());
                        break;

                    }
                    case "acceptrequest": {
                        String username1 = data.get("username1");
                        String username2 = data.get("username2");
                        String header = "";
                        JsonObject jsonObject = new JsonObject();
                        if (dbc.makeFriend(username1, username2)) {
                            dbc.removeAddFriend(username1, username2);

                            ArrayList<User> newFriendList = dbc.getUserFriends(username1);

                            header = "acceptrequested";
                            JsonArray friendsArray = new JsonArray();
                            for (User user : newFriendList) {
                                JsonObject friendJson = new JsonObject();
                                friendJson.addProperty("username", user.getUsername());
                                friendJson.addProperty("fullname", user.getFullname());
                                friendJson.addProperty("address", user.getAddress());
                                friendJson.addProperty("email", user.getEmail());
                                friendJson.addProperty("dob", user.getDob());
                                friendJson.addProperty("gender", user.getGender());
                                friendsArray.add(friendJson);
                            }
                            jsonObject.add("friends", friendsArray);
                        } else {
                            header = "noacceptrequest";
                        }
                        jsonObject.addProperty("header", header);
                        pw.println(jsonObject.toString());
                        break;
                    }

                    case "rejectrequest": {
                        String username1 = data.get("username1");
                        String username2 = data.get("username2");
                        String header = "";
                        JsonObject jsonObject = new JsonObject();
                        if (dbc.removeAddFriend(username1, username2)) {

                            header = "rejectrequested";

                        } else {
                            header = "noacceptrequest";
                        }
                        jsonObject.addProperty("header", header);
                        pw.println(jsonObject.toString());
                        break;
                    }
                    case "unfriend": {
                        String username1 = data.get("username1");
                        String username2 = data.get("username2");
                        String header = "";
                        JsonObject jsonResponseObject = new JsonObject();
                        if (dbc.removeFriend(username1, username2)) {
                            header = "unfriended";
                            ArrayList<User> newFriendList = dbc.getUserFriends(username1);
                            JsonArray friendsArray = new JsonArray();
                            for (User user : newFriendList) {
                                JsonObject friendJson = new JsonObject();
                                friendJson.addProperty("username", user.getUsername());
                                friendJson.addProperty("fullname", user.getFullname());
                                friendJson.addProperty("address", user.getAddress());
                                friendJson.addProperty("email", user.getEmail());
                                friendJson.addProperty("dob", user.getDob());
                                friendJson.addProperty("gender", user.getGender());
                                friendsArray.add(friendJson);
                            }
                            jsonResponseObject.add("friends", friendsArray);
                        } else {
                            header = "nounfriend";
                        }
                        jsonResponseObject.addProperty("header", header);
                        pw.println(jsonResponseObject.toString());
                        break;
                    }
                    case "block": {
                        String username1 = data.get("username1");
                        String username2 = data.get("username2");
                        String header = "";
                        JsonObject jsonResponseObject = new JsonObject();
                        if (dbc.blockFriend(username1, username2)) {
                            dbc.removeFriend(username1, username2);
                            header = "blocked";
                            ArrayList<User> newFriendList = dbc.getUserFriends(username1);
                            JsonArray friendsArray = new JsonArray();
                            for (User user : newFriendList) {
                                JsonObject friendJson = new JsonObject();
                                friendJson.addProperty("username", user.getUsername());
                                friendJson.addProperty("fullname", user.getFullname());
                                friendJson.addProperty("address", user.getAddress());
                                friendJson.addProperty("email", user.getEmail());
                                friendJson.addProperty("dob", user.getDob());
                                friendJson.addProperty("gender", user.getGender());
                                friendsArray.add(friendJson);
                            }
                            jsonResponseObject.add("friends", friendsArray);
                        } else {
                            header = "noblock";
                        }
                        jsonResponseObject.addProperty("header", header);
                        pw.println(jsonResponseObject.toString());
                        break;
                    }
                    case "deletechat": {
                        String username1 = data.get("username1");
                        String username2 = data.get("username2");
                        String header = "";
                        JsonObject jsonResponseObject = new JsonObject();
                        if (dbc.removeAllChat(username1, username2)) {

                            header = "deletechated";

                        } else {
                            header = "nodeletechat";
                        }
                        jsonResponseObject.addProperty("header", header);
                        pw.println(jsonResponseObject.toString());
                        break;
                    }
                    case "getallusers": {
                        ArrayList<User> users = dbc.getAllUsers();

                        String header = "getallusersed";
                        JsonArray usersArray = new JsonArray();
                        JsonObject jsonResponseObject = new JsonObject();
                        for (User user : users) {
                            JsonObject userJson = new JsonObject();
                            userJson.addProperty("username", user.getUsername());
                            userJson.addProperty("fullname", user.getFullname());
                            userJson.addProperty("address", user.getAddress());
                            userJson.addProperty("email", user.getEmail());
                            userJson.addProperty("dob", user.getDob());
                            userJson.addProperty("gender", user.getGender());
                            userJson.addProperty("createDate", user.getCreateDate());
                            usersArray.add(userJson);
                        }
                        jsonResponseObject.add("users", usersArray);
                        jsonResponseObject.addProperty("header", header);
                        pw.println(jsonResponseObject.toString());
                        break;
                    }
                    case "getlogindata": {
                        ArrayList<LoginLog> logs = dbc.getLoginData();

                        String header = "getlogindataed";
                        JsonArray logsArray = new JsonArray();
                        JsonObject jsonResponseObject = new JsonObject();
                        for (LoginLog log : logs) {
                            JsonObject logJson = new JsonObject();
                            logJson.addProperty("username", log.getUsername());
                            logJson.addProperty("loginTime", log.getLoginTime());
                            logJson.addProperty("fullname", log.getFullname());

                            logsArray.add(logJson);
                        }
                        jsonResponseObject.add("logs", logsArray);
                        jsonResponseObject.addProperty("header", header);
                        pw.println(jsonResponseObject.toString());
                        break;
                    }
                    default:
                        break;
                }
            } catch (IOException e) {

                e.printStackTrace();
            } finally {

            }

        }

        static String passwordUsingName(String Name) {

            String initials = Name.substring(0, 1).toUpperCase() + Name.substring(1, Math.min(Name.length(), 4));

            Random random = new Random();
            int randomNumber = 1000 + random.nextInt(9000);

            String generatedString = initials + randomNumber;

            return generatedString;
        }

    }

    class ChatSocketThread extends Thread {
        private Socket chatSocket;
        private String name;
        private BufferedReader br;
        private PrintWriter pw;

        ChatSocketThread(Socket chatSocket, String name) {
            this.chatSocket = chatSocket;
            this.name = name;
            try {

                br = new BufferedReader(new InputStreamReader(this.chatSocket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        String getSocketThreadName() {
            return name;
        }

        Socket getChatSocket() {
            return chatSocket;
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = br.readLine()) != null) {

                    Gson gson = new Gson();
                    ChatMessage msg = gson.fromJson(message, ChatMessage.class);

                    for (ChatSocketThread sct : chatClients) {
                        if (sct.getSocketThreadName().equals(msg.getTo())) {
                            pw = new PrintWriter(sct.getChatSocket().getOutputStream(), true);
                            pw.println(message);
                            break;
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    void sendEmail(String to, String subject, String body) {

        String username = dotenv.get("username");
        String password = dotenv.get("password");
        String from = username;

        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(username, password);

            }

        });

        session.setDebug(true);

        try {

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject(subject);

            message.setContent(
                    body,
                    "text/html");

            Transport.send(message);

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

}
