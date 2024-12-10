package bytecodeblinder.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import com.google.gson.Gson;

import bytecodeblinder.user.ChatMessage;
import bytecodeblinder.user.User;
import io.github.cdimascio.dotenv.Dotenv;

public class SocketController {
    private String serverIP;
    private int serverPort;
    private int serverChatPort;
    private Socket sSocket;
    private Socket cSocket;
    private Dotenv dotenv = Dotenv.load();

    BufferedReader br;
    PrintWriter pw;
    BufferedReader chatBR;
    PrintWriter chatPW;

    public SocketController() {

        this.serverIP = dotenv.get("serverIP");
        this.serverPort = Integer.parseInt(dotenv.get("serverPort"));
        this.serverChatPort = Integer.parseInt(dotenv.get("serverChatPort"));

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

    public void sendRequest(String data) {
        pw.println(data);

    }

    public String getResponse() {
        try {
            System.out.println("test2");
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void close() {
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

    public PrintWriter getChatWriter() {
        while (chatPW == null)
            ;
        return chatPW;
    }

    public void openChatSocket(User user) {
        new Thread(() -> {
            try {

                cSocket = new Socket(this.serverIP, this.serverChatPort);
                chatBR = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                chatPW = new PrintWriter(cSocket.getOutputStream(), true);

                System.out.println("chatPW" + chatPW);
                String message;
                try {
                    System.out.println("waiting");

                    while ((message = chatBR.readLine()) != null) {

                        Gson gson = new Gson();
                        ChatMessage msg = gson.fromJson(message, ChatMessage.class);

                        user.addMessage(msg);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }).start();

    }

}
