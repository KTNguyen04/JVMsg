package server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

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
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            // TODO: handle exception
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
    private Socket communicateSocket;

    ClientHandler(Socket communicateSocket) {
        this.communicateSocket = communicateSocket;
    }

    public void run() {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(communicateSocket.getInputStream()));
                PrintWriter pw = new PrintWriter(communicateSocket.getOutputStream())) {

            String str = br.readLine();
            System.out.println("message= " + str);
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

}