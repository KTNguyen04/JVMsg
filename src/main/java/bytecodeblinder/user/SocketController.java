package bytecodeblinder.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class SocketController {
    private String serverIP;
    private int serverPort;
    private int serverChatPort;
    private Socket sSocket;
    private Socket cSocket;
    // private CountDownLatch latch = new CountDownLatch(1);

    private Consumer<ChatMessage> addMessageCallback;

    int chatPort;

    BufferedReader br;
    PrintWriter pw;
    BufferedReader chatBR;
    PrintWriter chatPW;

    SocketController() {
        this("localhost", 7418); //
        this.serverChatPort = 8147;
    }

    SocketController(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            System.out.println("avs");

            sSocket = new Socket(serverIP, serverPort);
            System.out.println("avs");
            System.out.println(sSocket.getInetAddress());
            System.out.println(sSocket.getLocalAddress());
            br = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
            pw = new PrintWriter(sSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
            if (sSocket != null && !sSocket.isClosed()) {
                try {
                    System.out.println("dong");
                    sSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            if (pw != null) {
                System.out.println("close pw");
                pw.close();
            }

        }

    }

    void setAddMessageCallback(Consumer<ChatMessage> addMessageCallback) {
        this.addMessageCallback = addMessageCallback;
    }

    void sendRequest(String data) {
        pw.println(data);

    }

    String getResponse() {
        try {
            System.out.println("test2");
            return br.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    void close() {
        if (sSocket != null && !sSocket.isClosed()) {
            try {
                System.out.println("dadong");
                sSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if (br != null)
            try {
                br.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        if (pw != null)
            pw.close();

    }

    // void chatting(String message)
    PrintWriter getChatWriter() {
        while (chatPW == null)
            ;
        return chatPW;
    }

    void openChatSocket() {
        new Thread(() -> {
            try {

                cSocket = new Socket(this.serverIP, this.serverChatPort);
                chatBR = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                chatPW = new PrintWriter(cSocket.getOutputStream(), true);
                // latch.countDown();
                System.out.println("chatPW" + chatPW);
                String message;
                try {
                    System.out.println("waiting");
                    // chatPW.println("from client");
                    while ((message = chatBR.readLine()) != null) {

                        Gson gson = new Gson();
                        ChatMessage msg = gson.fromJson(message, ChatMessage.class);

                        UserView.user.addMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }).start();

    }

    // class ChatHandler extends Thread {

    // private Socket chatSocket;

    // ChatHandler(Socket chatSocket) {
    // // dbc = new DatabaseController();
    // this.chatSocket = chatSocket;
    // }

    // public void run() {

    // try (BufferedReader br = new BufferedReader(new
    // InputStreamReader(chatSocket.getInputStream()));
    // PrintWriter pw = new PrintWriter(chatSocket.getOutputStream(), true)) {

    // do {
    // String json = br.readLine();
    // System.out.println("message= " + json);
    // Gson gson = new Gson();
    // HashMap<String, String> data = gson.fromJson(json, HashMap.class);
    // System.out.println("chat" + data.get("header"));
    // switch (data.get("header")) {
    // case "signup":
    // System.out.println("Signuphandle");

    // break;

    // default:
    // break;
    // }
    // } while (true);

    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } finally {
    // try {

    // chatSocket.close();
    // // implement closing all socket here
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    // }

    // // String getUserData(String username) {
    // // String user = dbc.getUser(username);
    // // System.out.println(user);
    // // return "";
    // // }

    // }

}
