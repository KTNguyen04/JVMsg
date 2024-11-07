package server;

import javax.imageio.ImageIO;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;

import com.toedter.calendar.JDateChooser;

import java.util.Date;
import java.util.LinkedList;

import config.AppConfig;

class ServerView {
    private JFrame frame;
    private JScrollPane scrollPanel;
    private JPanel panel;

    private Server server;

    ServerView(Server sv) {

        server = sv;

        Integer width = Integer.valueOf(AppConfig.serverWidth);
        Integer height = Integer.valueOf(AppConfig.serverHeight);

        frame = new JFrame(AppConfig.serverTitle);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // center the frame

        frame.setResizable(false);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        scrollPanel = new JScrollPane(panel);
        scrollPanel.setPreferredSize(new Dimension(300, 200));

        // label.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
        // panel.add(label);
        // cardLO.show(panel, "LOGIN");
        // new SignupPanel(panel);

        JToggleButton runServerBtn = new JToggleButton("Run Server");
        runServerBtn.setFont(new Font("Nunito Sans", Font.BOLD, 20));
        runServerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        runServerBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ae) {

                if (runServerBtn.isSelected()) {
                    server.run();
                    messageHandler("Server IP address: " + server.getServerIP());
                    messageHandler("Waiting for client");
                    runServerBtn.setText("Close Server");
                } else {
                    server.close();
                    runServerBtn.setText("Run Server");
                }

            }
        });

        panel.add(runServerBtn);
        frame.add(scrollPanel);
        frame.setVisible(true);

    }

    void messageHandler(String msg) {
        JLabel label = new JLabel(msg);
        label.setFont(new Font("Nunito Sans", Font.PLAIN, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.revalidate();
        panel.repaint();

    }
}
