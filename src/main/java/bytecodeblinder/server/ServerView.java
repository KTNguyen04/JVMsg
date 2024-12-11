package bytecodeblinder.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import io.github.cdimascio.dotenv.Dotenv;

class ServerView {
    private JFrame frame;
    private JScrollPane scrollPanel;
    private JPanel panel;

    private Server server;
    private Dotenv dotenv = Dotenv.load();

    ServerView(Server sv) {

        server = sv;

        Integer width = Integer.valueOf(dotenv.get("server.width"));
        Integer height = Integer.valueOf(dotenv.get("server.height"));

        frame = new JFrame(dotenv.get("server.title"));
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setResizable(false);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        scrollPanel = new JScrollPane(panel);
        scrollPanel.setPreferredSize(new Dimension(300, 200));

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
