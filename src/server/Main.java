package server;

import java.io.*;
import java.sql.*;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] agrs) {
        Server server = new Server();

        SwingUtilities.invokeLater(() -> {
            ServerView serverView = new ServerView(server);
        });

    }
}
