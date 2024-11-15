package server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

import user.Message;
import user.User;

class SocketController {
    private int port;
    private int chatPort;

    private ServerSocket listenSocket;

    private String justForTest;
    private ArrayList<ChatSocketThread> chatClients;
    private int numberClients;
    private DatabaseController dbc;

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
                    case "signup":
                        System.out.println("Signuphandle");
                        User usr = gson.fromJson(json, User.class);
                        dbc.insertUser(usr);

                        // dbc.insertUser()

                        break;
                    case "login":
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
                        } else {
                            JsonObject jsonResponseObject = new JsonObject();
                            jsonResponseObject.addProperty("header", "nologin");
                            String jsonResponse = gson.toJson(jsonResponseObject);
                            pw.println(jsonResponse);

                        }

                        break;

                    case "messages":
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
                    case "chat":
                        // Gson gson = new Gson();
                        Message msg = gson.fromJson(json, Message.class);
                        dbc.insertMessage(msg);

                        // dbc.insertMessage(msg);

                        System.out.println("cte" + data.get("content"));
                        break;

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
                    Message msg = gson.fromJson(message, Message.class);
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

}
