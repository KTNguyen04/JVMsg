package bytecodeblinder.server;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

import bytecodeblinder.config.AppConfig;

import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import bytecodeblinder.user.*;

class SocketController {
    private int port;
    private int chatPort;

    private ServerSocket listenSocket;

    private String justForTest;
    private ArrayList<ChatSocketThread> chatClients;
    private int numberClients;
    private DatabaseController dbc;
    private Dotenv dotenv = Dotenv.load();

    String curAccepted;

    SocketController() {
        this.port = 7418;
        this.chatPort = 8147;
        chatClients = new ArrayList<>();
        numberClients = 0;

        dbc = new DatabaseController();

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
            System.out.println("try to open ");

            new Thread(() -> {
                try (ServerSocket lisSocket = new ServerSocket(chatPort);) {
                    while (!listenSocket.isClosed()) {

                        Socket communicateSocket = lisSocket.accept();
                        System.out.println(numberClients);

                        System.out.println("Chattttt");
                        ChatSocketThread cst = new ChatSocketThread(communicateSocket, curAccepted);
                        cst.start();
                        chatClients.add(cst);

                        // System.out.println("Accepted");
                        // new ClientHandler(communicateSocket).start();

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

                        System.out.println("Accepted");
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
        System.out.println(justForTest);
        if (!listenSocket.isClosed()) {
            try {
                System.out.println("closrr sev");
                listenSocket.close();
                // System.out.println("et");

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
                System.out.println("message= " + json);
                Gson gson = new Gson();
                HashMap<String, String> data = gson.fromJson(json, HashMap.class);

                // for (Map.Entry<String, String> me : data.entrySet()) {
                // System.out.print(me.getKey() + ": ");
                // System.out.println(me.getValue());
                // }

                switch (data.get("header")) {
                    case "signup": {
                        System.out.println("Signuphandle");
                        User usr = gson.fromJson(json, User.class);
                        String header = "";
                        if (dbc.insertUser(usr)) {
                            header = "signuped";
                        } else {
                            System.out.println("aba");
                            header = "nosignup";
                        }
                        String wrappedJson = gson.toJson(Map.of("header", header));
                        pw.println(wrappedJson);

                        // dbc.insertUser()

                        break;
                    }
                    case "login": {
                        String username = data.get("username");
                        String password = data.get("password");
                        if (dbc.checkLogin(username, password)) {
                            System.out.println("Login thanh con");
                            String header = "logined";

                            String userData = dbc.getUserData(username);
                            // String userFriends = dbc.getUserFriends(username);

                            JsonObject jsonObject = JsonParser.parseString(userData).getAsJsonObject();

                            jsonObject.addProperty("header", header);

                            String jsonResponse = gson.toJson(jsonObject);
                            pw.println(jsonResponse);

                            System.out.println(jsonResponse);

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
                        // String userFriends = dbc.getUserFriends(username);
                        // String header = "messagesed";

                        // JsonArray jsonArray = gson.fromJson(messages, JsonArray.class);

                        // // jsonArray.addProperty("header", header);

                        // String jsonResponse = gson.toJson(jsonObject);
                        pw.println(messages);

                        System.out.println(messages);

                        System.out.println("message test");
                        break;
                    }
                    case "chat": {
                        // Gson gson = new Gson();
                        ChatMessage msg = gson.fromJson(json, ChatMessage.class);
                        dbc.insertMessage(msg);

                        // dbc.insertMessage(msg);

                        System.out.println("cte" + data.get("content"));
                        break;
                    }
                    case "edit": {
                        // Gson gson = new Gson();
                        System.out.println(json);
                        String header = "";
                        User usr = gson.fromJson(json, User.class);
                        if (dbc.editUser(usr, data.get("newPassword"))) {
                            header = "edited";

                        } else {
                            header = "noedit";
                        }

                        String userData = dbc.getUserData(usr.getUsername());
                        // String userFriends = dbc.getUserFriends(username);

                        JsonObject jsonObject = JsonParser.parseString(userData).getAsJsonObject();

                        jsonObject.addProperty("header", header);

                        String jsonResponse = gson.toJson(jsonObject);

                        pw.println(jsonResponse);

                        // dbc.insertMessage(msg);

                        // dbc.insertMessage(msg);

                        System.out.println("cte" + data.get("content"));
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
                            System.out.println("reset");
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

                    }
                    case "offline": {
                        String username = data.get("username");
                        dbc.removeOnline(username);
                    }
                    default:
                        break;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                // try {

                // // communicateSocket.close();
                // // implement closing all socket here
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
            }

        }

        static String passwordUsingName(String Name) {

            String initials = Name.substring(0, 1).toUpperCase() + Name.substring(1, Math.min(Name.length(), 4));

            Random random = new Random();
            int randomNumber = 1000 + random.nextInt(9000);

            String generatedString = initials + randomNumber;

            return generatedString;
        }

        // String getUserData(String username) {
        // String user = dbc.getUser(username);
        // System.out.println(user);
        // return "";
        // }

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
                System.out.println("sve");
                String message;
                while ((message = br.readLine()) != null) {
                    // String ;
                    System.out.println(message);
                    Gson gson = new Gson();
                    ChatMessage msg = gson.fromJson(message, ChatMessage.class);
                    // System.out.println("thr" + msg.getContent() + msg.getTimeStamp());
                    for (ChatSocketThread sct : chatClients) {
                        System.out.println(sct.getSocketThreadName());
                        if (sct.getSocketThreadName().equals(msg.getTo())) {
                            pw = new PrintWriter(sct.getChatSocket().getOutputStream(), true);
                            pw.println(message);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    void sendEmail(String to, String subject, String body) {

        // Sender's email ID needs to be mentioned
        String username = dotenv.get("username");
        String password = dotenv.get("password");
        String from = username;
        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(username, password);

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            // message.setText("This is actual message");

            // Send the actual HTML message.
            message.setContent(
                    body,
                    "text/html");

            // System.out.println("sending...");
            // Send message
            Transport.send(message);
            // System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

}
