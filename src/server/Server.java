package server;

import java.net.InetAddress;
import java.net.UnknownHostException;

class Server {
    SocketController sc;

    Server() {

        sc = new SocketController();

    };

    String getServerIP() {
        return sc.getServerIP();
    }

    void run() {
        sc.openSocket();
        sc.openChatSocket();
    }

    void close() {
        sc.closeSocket();
    }

}
