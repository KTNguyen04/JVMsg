package user;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

class SocketController {
    String serverIP;
    int serverPort;
    String IP;
    int port;
    Socket socket;

    BufferedReader br;
    PrintWriter pw;

    SocketController() {
        this("localhost", 7418); //
    }

    SocketController(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverIP, serverPort);
            System.out.println(socket);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
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
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
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

}
