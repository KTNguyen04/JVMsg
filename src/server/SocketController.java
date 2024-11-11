package server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.Gson;

class SocketController {
    private int port;

    private ServerSocket listenSocket;

    private String justForTest;

    SocketController() {
        this.port = 7418;

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
                listenSocket.close();
                // System.out.println("et");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {

    private DatabaseController dbc;

    private Socket communicateSocket;

    ClientHandler(Socket communicateSocket) {
        dbc = new DatabaseController();
        this.communicateSocket = communicateSocket;
    }

    public void run() {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(communicateSocket.getInputStream()));
                PrintWriter pw = new PrintWriter(communicateSocket.getOutputStream())) {

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

                    break;
                case "login":
                    String username = data.get("username");
                    String password = data.get("password");
                    if (dbc.checkLogin(username, password)) {
                        System.out.println("Login thanh con");
                    }

                    break;

                default:
                    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                communicateSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // String getUserData() {

    // }

}