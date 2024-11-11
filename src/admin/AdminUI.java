package admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.gson.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;

import java.util.*;

import config.AppConfig;

class AdminView {
    private JFrame frame;
    private JPanel panel;
    // private CardLayout cardLO;
    // private SocketController socketController;
    // Mode mode;

    // User user;

    AdminView() throws IOException {

        Integer width = Integer.valueOf(AppConfig.appWidth);
        Integer height = Integer.valueOf(AppConfig.appHeight);

        frame = new JFrame(AppConfig.appName);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout());

        frame.setLocationRelativeTo(null); // center the frame

        frame.setResizable(false);

        // cardLO = new CardLayout();
        panel = new MainPanel();

        // panel.add("LOGIN", new MainPanel());
        // panel.add("SIGNUP", new SignupPanel());
        // panel.add("HOME", new HomePanel());

        // cardLO.show(panel, "LOGIN");
        // new SignupPanel(panel);
        frame.add(panel);
        frame.setVisible(true);
    }

    class MainPanel extends JPanel {
        // private JTextField usernameField;
        // private JTextField passwordField;
        // private JLabel messageHolder;

        MainPanel() throws IOException {
        }
        // super(new BorderLayout());

        // JPanel leftPanel = new JPanel();
        // JPanel rightPanel = new JPanel();

        // rightPanel.setBackground(Color.WHITE);
        // rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath));
        // Image scaledImage = bannerImage.getScaledInstance(AppConfig.bannerWidth,
        // AppConfig.bannerHeight,
        // Image.SCALE_SMOOTH);
        // JLabel banner = new JLabel(new ImageIcon(scaledImage));

        // rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // JLabel loginTitle = new JLabel("Login to your account");
        // loginTitle.setForeground(new Color(82, 82, 82));

        // loginTitle.setFont(new Font("Nunito Sans", Font.BOLD, 36));

        // JLabel usernameLabel = new JLabel("Username");
        // usernameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

        // usernameField = new JTextField(40);
        // usernameField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
        // usernameField.setMaximumSize(usernameField.getPreferredSize());
        // usernameField.setMargin(new Insets(5, 5, 5, 5));
        // // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // JLabel passwordLabel = new JLabel("Password");
        // passwordLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
        // passwordLabel.setForeground(new Color(82, 82, 82));

        // // passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // passwordField = new JPasswordField(40);
        // passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        // passwordField.setMargin(new Insets(5, 5, 5, 5));
        // passwordField.setMaximumSize(passwordField.getPreferredSize());

        // JButton submitButton = new JButton("Login");
        // submitButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
        // submitButton.setBackground(new Color(127, 38, 91));
        // submitButton.setForeground(Color.WHITE);
        // submitButton.setPreferredSize(new Dimension(380, 40));
        // submitButton.setMaximumSize(new Dimension(380, 40));
        // submitButton.addMouseListener(new MouseAdapter() {
        // public void mouseClicked(MouseEvent me) {
        // new Thread(() -> {
        // SocketController sc = new SocketController();
        // String packet = getLogInData();
        // System.out.println(packet);
        // sc.sendRequest(packet);

        // System.out.println("test");
        // String res = sc.getResponse();
        // Gson gson = new Gson();

        // System.out.println(res);
        // sc.close();
        // JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
        // String resHeader = jsonObject.get("header").getAsString();
        // if (resHeader.equals("logined")) {
        // user = gson.fromJson(res, User.class);
        // cardLO.show(panel, "HOME");

        // } else {
        // messageHolder.setText("LOGIN FAILED");
        // messageHolder.setForeground(randomColor());
        // }

        // // cardLO.show(panel, "HOME");
        // }).start();
        // }
        // });

        // JPanel registerInstruction = new JPanel();
        // JLabel questionText = new JLabel("Not Registered Yet?");
        // JLabel instructionText = new JLabel("Create an account");
        // questionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        // questionText.setForeground(new Color(82, 82, 82));
        // instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        // instructionText.setForeground(new Color(127, 38, 91));
        // instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // registerInstruction.setMaximumSize(new Dimension(400, 50));
        // registerInstruction.setBackground(Color.WHITE);
        // registerInstruction.add(questionText);
        // registerInstruction.add(instructionText);
        // registerInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);

        // instructionText.addMouseListener(new MouseAdapter() {
        // public void mouseEntered(MouseEvent me) {
        // instructionText.setText("<html><u>Create an account</u></html>");
        // }

        // public void mouseExited(MouseEvent me) {
        // instructionText.setText("<html>Create an account</html>");
        // }

        // public void mouseClicked(MouseEvent me) {
        // cardLO.show(panel, "SIGNUP");
        // }

        // });

        // messageHolder = new JLabel(" ");

        // messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        // messageHolder.setForeground(Color.decode("#ffc107"));

        // // instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        // // instructionText.setForeground(new Color(127, 38, 91));
        // // instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // leftPanel.add(banner);

        // rightPanel.add(Box.createVerticalGlue());
        // rightPanel.add(loginTitle);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        // // rightPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        // rightPanel.add(usernameLabel);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // rightPanel.add(usernameField);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        // rightPanel.add(passwordLabel);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        // rightPanel.add(passwordField);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        // rightPanel.add(submitButton);
        // rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        // rightPanel.add(registerInstruction);

        // rightPanel.add(messageHolder);
        // rightPanel.add(Box.createVerticalGlue());

        // add(leftPanel, BorderLayout.WEST);
        // add(rightPanel, BorderLayout.CENTER);
        // }

        // String getLogInData() {
        // HashMap<String, String> loginData = new HashMap<>();

        // String username = usernameField.getText();
        // String password = passwordField.getText();

        // loginData.put("header", "login");
        // loginData.put("username", username);
        // loginData.put("password", password);

        // Gson gson = new Gson();
        // String json = gson.toJson(loginData);
        // return json;

        // }

        // public static Color randomColor() {
        // // Tạo đối tượng Random
        // Random rand = new Random();

        // int red = rand.nextInt(256);
        // int green = rand.nextInt(256);
        // int blue = rand.nextInt(256);

        // return new Color(red, green, blue);
        // }

        // // void sendLoginRequest() {
        // // System.out.println(usernameField.getText());
        // // System.out.println(passwordField.getText());
        // // }
    }

}
