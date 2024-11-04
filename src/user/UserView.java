package user;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;

import config.AppConfig;

class UserView {
    private JFrame frame;
    private JPanel panel;
    Mode mode;

    enum Mode {
        LOGIN, SIGNUP, HOME
    }

    UserView() throws IOException {
        Integer width = Integer.valueOf(AppConfig.appWidth);
        Integer height = Integer.valueOf(AppConfig.appHeight);

        frame = new JFrame(AppConfig.appName);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout());

        frame.setLocationRelativeTo(null); // center the frame
        mode = Mode.LOGIN;
        panel = new LoginPanel();
        frame.add(panel);
        frame.setVisible(true);
    }

}

class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JTextField passwordField;

    LoginPanel() throws IOException {

        super(new BorderLayout());

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath));
        Image scaledImage = bannerImage.getScaledInstance(AppConfig.bannerWidth, AppConfig.bannerHeight,
                Image.SCALE_SMOOTH);
        JLabel banner = new JLabel(new ImageIcon(scaledImage));

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel loginTitle = new JLabel("Login to your account");
        loginTitle.setForeground(new Color(82, 82, 82));

        loginTitle.setFont(new Font("Nunito Sans", Font.BOLD, 36));

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

        usernameField = new JTextField(40);
        usernameField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
        usernameField.setMaximumSize(usernameField.getPreferredSize());
        usernameField.setMargin(new Insets(5, 5, 5, 5));
        // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
        passwordLabel.setForeground(new Color(82, 82, 82));

        // passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField(40);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        passwordField.setMaximumSize(passwordField.getPreferredSize());

        JButton submitButton = new JButton("Login");
        submitButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
        submitButton.setBackground(new Color(127, 38, 91));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(380, 40));
        submitButton.setMaximumSize(new Dimension(380, 40));

        JPanel registerInstruction = new JPanel();
        JLabel questionText = new JLabel("Not Registered Yet?");
        JLabel instructionText = new JLabel("Create an account");
        questionText.setFont(new Font("Nunito Sans", Font.BOLD, 16));
        questionText.setForeground(new Color(82, 82, 82));
        instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 16));
        instructionText.setForeground(new Color(127, 38, 91));
        instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        instructionText.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                instructionText.setText("<html><u>Create an account</u></html>");
            }

            public void mouseExited(MouseEvent me) {
                instructionText.setText("<html>Create an account</html>");
            }
        });
        registerInstruction.setMaximumSize(new Dimension(400, 50));
        registerInstruction.setBackground(Color.WHITE);
        registerInstruction.add(questionText);
        registerInstruction.add(instructionText);
        registerInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(banner);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(loginTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        // rightPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        rightPanel.add(usernameLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        rightPanel.add(usernameField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        rightPanel.add(submitButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        rightPanel.add(registerInstruction);
        rightPanel.add(Box.createVerticalGlue());

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

}

class SignupPanel extends JPanel {

    SignupPanel() throws IOException {
        super(new BorderLayout());
        BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath));
        Image scaledImage = bannerImage.getScaledInstance(AppConfig.bannerWidth, AppConfig.bannerHeight,
                Image.SCALE_SMOOTH);
        JLabel banner = new JLabel(new ImageIcon(scaledImage));
        add(banner, BorderLayout.WEST);
    }

}