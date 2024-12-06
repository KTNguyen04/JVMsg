package bytecodeblinder.user;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.lang.reflect.Type;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

// import com.google.gson.reflect.TypeToken;
// import com.google.gson.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;

import com.toedter.calendar.JDateChooser;
import java.util.*;
import java.util.Date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.github.cdimascio.dotenv.Dotenv;

class UserView {
    private JFrame frame;
    private JPanel panel;
    private CardLayout cardLO;
    private PrintWriter chatPW;

    private Mode mode;

    static User user;

    private Dotenv dotenv = Dotenv.load();

    private enum Mode {
        LOGIN, SIGNUP, HOME
    }

    private String imagePath = dotenv.get("image.path");

    UserView() throws IOException {

        Integer width = Integer.valueOf(dotenv.get("app.width"));
        Integer height = Integer.valueOf(dotenv.get("app.height"));

        frame = new JFrame(dotenv.get("app.title"));
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout());

        frame.setLocationRelativeTo(null); // center the frame

        frame.setResizable(false);

        cardLO = new CardLayout();
        panel = new JPanel(cardLO);

        panel.add("LOGIN", new LoginPanel());
        panel.add("SIGNUP", new SignupPanel());
        // panel.add("HOME", new HomePanel());

        cardLO.show(panel, "LOGIN");
        mode = Mode.LOGIN;
        // new SignupPanel(panel);
        frame.add(panel);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (mode == Mode.HOME)
                    sendOffline();
                System.out.println("test");
            }
        });

        UIManager.put("ToolTip.font", new Font("Arial", Font.BOLD, 16));
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setInitialDelay(50);
        toolTipManager.setReshowDelay(0);

    }

    void sendOffline() {
        System.out.println("LOG");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("header", "offline");
        jsonObject.addProperty("username", user.getUsername());

        SocketController sc = new SocketController();

        sc.sendRequest(jsonObject.toString());
        sc.close();

    }

    private class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JTextField passwordField;
        private JLabel messageHolder;

        LoginPanel() throws IOException {

            super(new BorderLayout());

            JPanel leftPanel = new JPanel();
            JPanel rightPanel = new JPanel();

            rightPanel.setBackground(Color.WHITE);
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            BufferedImage bannerImage = ImageIO.read(new File(imagePath + dotenv.get("banner")));
            Image scaledImage = bannerImage.getScaledInstance(Integer.parseInt(dotenv.get("banner.width")),
                    Integer.parseInt(dotenv.get("banner.height")),
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

            JLabel forgetText = new JLabel("Forgot your password?");

            forgetText.setFont(new Font("Nunito Sans", Font.BOLD, 15));
            forgetText.setForeground(new Color(127, 38, 91));
            forgetText.setCursor(new Cursor(Cursor.HAND_CURSOR));

            forgetText.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent me) {
                    forgetText.setText("<html><u>Forgot your password?</u></html>");
                }

                public void mouseExited(MouseEvent me) {
                    forgetText.setText("<html>Forgot your password?</html>");
                }

                public void mouseClicked(MouseEvent me) {
                    JDialog resetPassDialog = createResetPasswordDialog();
                    resetPassDialog.setVisible(true);
                    // cardLO.show(panel, "SIGNUP");
                }

            });

            JButton submitButton = new JButton("Login");
            submitButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            submitButton.setBackground(new Color(127, 38, 91));
            submitButton.setForeground(Color.WHITE);
            submitButton.setPreferredSize(new Dimension(380, 40));
            submitButton.setMaximumSize(new Dimension(380, 40));
            submitButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        String packet = getLogInData();
                        String password = passwordField.getText();
                        // System.out.println(packet);
                        sc.sendRequest(packet);

                        System.out.println("test");
                        String res = sc.getResponse();
                        Gson gson = new Gson();

                        // System.out.println(res);
                        JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = jsonObject.get("header").getAsString();

                        if (resHeader.equals("logined")) {
                            // System.out.println("Break1");
                            user = gson.fromJson(res, User.class);
                            user.setPassword(password);
                            panel.add("HOME", new HomePanel());

                            cardLO.show(panel, "HOME");
                            mode = Mode.HOME;

                            sc.openChatSocket();
                            chatPW = sc.getChatWriter();
                            // // System.out.println("chatPW" + chatPW);
                            sc.close();

                            // System.out.println("Break2");

                        } else {
                            messageHolder.setText("LOGIN FAILED");
                            messageHolder.setForeground(randomColor());
                        }

                        // cardLO.show(panel, "HOME");
                    }).start();
                }
            });

            JPanel registerInstruction = new JPanel();
            JLabel questionText = new JLabel("Not Registered Yet?");
            JLabel instructionText = new JLabel("Create an account");
            questionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            questionText.setForeground(new Color(82, 82, 82));
            instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            instructionText.setForeground(new Color(127, 38, 91));
            instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));

            registerInstruction.setMaximumSize(new Dimension(400, 50));
            registerInstruction.setBackground(Color.WHITE);
            registerInstruction.add(questionText);
            registerInstruction.add(instructionText);
            registerInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);

            instructionText.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent me) {
                    instructionText.setText("<html><u>Create an account</u></html>");
                }

                public void mouseExited(MouseEvent me) {
                    instructionText.setText("<html>Create an account</html>");
                }

                public void mouseClicked(MouseEvent me) {
                    cardLO.show(panel, "SIGNUP");
                    mode = Mode.SIGNUP;
                }

            });

            messageHolder = new JLabel(" ");

            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            messageHolder.setForeground(Color.decode("#ffc107"));

            // instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            // instructionText.setForeground(new Color(127, 38, 91));
            // instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
            rightPanel.add(forgetText);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));
            rightPanel.add(submitButton);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 60)));

            rightPanel.add(registerInstruction);

            rightPanel.add(messageHolder);
            rightPanel.add(Box.createVerticalGlue());

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }
        // String createOnlinePacket(){
        // HashMap<String, String> data = new HashMap<>();

        // // loginData.put("header", "online");
        // // loginData.put("ip", );
        // // loginData.put("password", password);

        // Gson gson = new Gson();
        // String json = gson.toJson(loginData);
        // return json;
        // }
        String getLogInData() {
            HashMap<String, String> loginData = new HashMap<>();

            String username = usernameField.getText();
            String password = passwordField.getText();

            loginData.put("header", "login");
            loginData.put("username", username);
            loginData.put("password", password);

            Gson gson = new Gson();
            String json = gson.toJson(loginData);
            return json;

        }

        JDialog createResetPasswordDialog() {
            JDialog dialog = new JDialog(frame, "User info");
            JPanel dPanel = new JPanel();
            dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.Y_AXIS));

            JLabel confirmText = new JLabel("Send new password through your email?");
            confirmText.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            confirmText.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel usrnLabel = new JLabel("Enter your username");
            usrnLabel.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            usrnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextField usrnField = new JTextField();
            usrnField.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            usrnField.setAlignmentX(Component.CENTER_ALIGNMENT);
            usrnField.setMaximumSize(new Dimension(400, 35));

            JLabel emailLabel = new JLabel("Enter your registered email");
            emailLabel.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextField emailField = new JTextField();
            emailField.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
            emailField.setMaximumSize(new Dimension(400, 35));

            JLabel messageHolder = new JLabel("");
            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            messageHolder.setForeground(Color.decode("#198754"));
            messageHolder.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton sendBtn = new JButton("Send");
            sendBtn.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            sendBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sendBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    HashMap<String, String> resetData = new HashMap<>();

                    String email = emailField.getText();
                    String usrn = usrnField.getText();

                    resetData.put("header", "reset");
                    resetData.put("username", usrn);
                    resetData.put("email", email);
                    Gson gson = new Gson();

                    String json = gson.toJson(resetData);

                    SocketController sc = new SocketController();

                    sc.sendRequest(json);
                    String res = sc.getResponse();
                    JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                    String resHeader = jsonObject.get("header").getAsString();

                    if (resHeader.equals("reseted")) {
                        messageHolder.setText("SENT");
                        messageHolder.setForeground(Color.decode("#198754"));

                    } else {
                        messageHolder.setText("CANNOT FIND YOUR USERNAME OR PASSWORD");
                        messageHolder.setForeground(randomColor());
                    }

                    sc.close();

                }
            });

            // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            dialog.setSize(500, 300);

            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            dPanel.add(confirmText);
            dPanel.add(usrnLabel);
            dPanel.add(usrnField);
            dPanel.add(emailLabel);
            dPanel.add(emailField);
            dPanel.add(sendBtn);
            dPanel.add(messageHolder);

            dialog.add(dPanel);

            dialog.setLocationRelativeTo(null); // center the frame

            dialog.setResizable(false);

            // System.out.println(user.getFriends().get(0));

            return dialog;
        }

        // void sendLoginRequest() {
        // System.out.println(usernameField.getText());
        // System.out.println(passwordField.getText());
        // }
    }

    private class SignupPanel extends JPanel {
        private JTextField usernameField;
        private JTextField passwordField;
        private JTextField fullnameField;
        private JTextField emailField;
        private JTextField addressField;
        private JDateChooser dobField;

        JRadioButton maleRadio;
        JRadioButton femaleRadio;

        // private JTextField addressField;
        // tên đăng nhập, họ tên, địa chỉ, ngàysinh, giới tính, emai
        SignupPanel() throws IOException {
            super(new BorderLayout());
            JPanel leftPanel = new JPanel();
            JPanel rightPanel = new JPanel();

            rightPanel.setBackground(Color.WHITE);
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            BufferedImage bannerImage = ImageIO.read(new File(imagePath + dotenv.get("banner")));
            Image scaledImage = bannerImage.getScaledInstance(Integer.parseInt(dotenv.get("banner.width")),
                    Integer.parseInt(dotenv.get("banner.height")),
                    Image.SCALE_SMOOTH);
            JLabel banner = new JLabel(new ImageIcon(scaledImage));

            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            JLabel signupTitle = new JLabel("Create your account");
            signupTitle.setForeground(new Color(82, 82, 82));

            signupTitle.setFont(new Font("Nunito Sans", Font.BOLD, 36));

            JLabel usernameLabel = new JLabel("Username");
            usernameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            usernameField = new JTextField(40);
            usernameField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            usernameField.setMaximumSize(usernameField.getPreferredSize());
            usernameField.setMargin(new Insets(5, 5, 5, 5));
            // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel fullnameLabel = new JLabel("Full name");
            fullnameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            fullnameField = new JTextField(40);
            fullnameField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            fullnameField.setMaximumSize(usernameField.getPreferredSize());
            fullnameField.setMargin(new Insets(5, 5, 5, 5));

            JLabel emailLabel = new JLabel("Email");
            emailLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            emailField = new JTextField(40);
            emailField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            emailField.setMaximumSize(usernameField.getPreferredSize());
            emailField.setMargin(new Insets(5, 5, 5, 5));
            // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel addressLabel = new JLabel("Address");
            addressLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            addressField = new JTextField(40);
            addressField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            addressField.setMaximumSize(usernameField.getPreferredSize());
            addressField.setMargin(new Insets(5, 5, 5, 5));

            JLabel dobLabel = new JLabel("Date of birth");
            dobLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
            dobField = new JDateChooser();
            // dobField.setPreferredSize(new Dimension(330, 80));40
            dobField.setMaximumSize(new Dimension(770, 45));
            dobField.setPreferredSize(new Dimension(770, 45));
            dobField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            dobField.setDate(new Date());
            // dobField.setMaximumSize(new Dimension(380, 80));

            JLabel genderLabel = new JLabel("Gender");
            genderLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            maleRadio = new JRadioButton("Male");
            femaleRadio = new JRadioButton("Female");
            maleRadio.setBackground(Color.WHITE);
            maleRadio.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            maleRadio.setSelected(true);
            femaleRadio.setBackground(Color.WHITE);
            femaleRadio.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            ButtonGroup bg = new ButtonGroup();
            bg.add(maleRadio);
            bg.add(femaleRadio);
            JPanel genderRow = new JPanel();
            genderRow.add(maleRadio);
            genderRow.add(femaleRadio);
            genderRow.setBackground(Color.WHITE);
            genderRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel passwordLabel = new JLabel("Password");
            passwordLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
            passwordLabel.setForeground(new Color(82, 82, 82));

            // passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            passwordField = new JPasswordField(40);
            passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
            passwordField.setMargin(new Insets(5, 5, 5, 5));
            passwordField.setMaximumSize(passwordField.getPreferredSize());

            JLabel messageHolder = new JLabel("");
            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            messageHolder.setForeground(Color.decode("#198754"));

            JButton submitButton = new JButton("Sign Up");
            submitButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            submitButton.setBackground(new Color(127, 38, 91));
            submitButton.setForeground(Color.WHITE);
            submitButton.setPreferredSize(new Dimension(380, 40));
            submitButton.setMaximumSize(new Dimension(380, 40));
            submitButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        String packet = getSignUpData();
                        sc.sendRequest(packet);

                        String res = sc.getResponse();
                        sc.close();
                        Gson gson = new Gson();

                        // System.out.println(res);
                        JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = jsonObject.get("header").getAsString();
                        if (resHeader.equals("signuped")) {
                            // System.out.println("Break1");

                            messageHolder.setText("SIGN UP SUCCESSFULLY");
                            messageHolder.setForeground(Color.decode("#198754"));

                            // System.out.println("Break2");

                        } else {
                            messageHolder.setText("SIGNUP FAILED");
                            messageHolder.setForeground(randomColor());
                        }

                        // cardLO.show(panel, "HOME");
                    }).start();
                }
            });

            JPanel loginInstruction = new JPanel();
            JLabel questionText = new JLabel("Have an account?");
            JLabel instructionText = new JLabel("Log in");
            questionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            questionText.setForeground(new Color(82, 82, 82));
            instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            instructionText.setForeground(new Color(127, 38, 91));
            instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));
            instructionText.addMouseListener(new MouseAdapter() {

                public void mouseEntered(MouseEvent me) {
                    instructionText.setText("<html><u>Log in</u></html>");
                }

                public void mouseExited(MouseEvent me) {
                    instructionText.setText("<html>Log in</html>");
                }

                public void mouseClicked(MouseEvent me) {
                    cardLO.show(panel, "LOGIN");
                    mode = Mode.LOGIN;
                }
            });
            loginInstruction.setMaximumSize(new Dimension(400, 50));
            loginInstruction.setBackground(Color.WHITE);
            loginInstruction.add(questionText);
            loginInstruction.add(instructionText);
            loginInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);

            leftPanel.add(banner);

            rightPanel.add(Box.createVerticalGlue());
            rightPanel.add(signupTitle);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(usernameLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(usernameField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(fullnameLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(fullnameField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(addressLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(addressField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(emailLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(emailField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(dobLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(dobField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(genderLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(genderRow);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(passwordLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(passwordField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            rightPanel.add(submitButton);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            rightPanel.add(messageHolder);
            // rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            rightPanel.add(loginInstruction);
            rightPanel.add(Box.createVerticalGlue());

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        String getSignUpData() {
            // HashMap<String, String> signupData = new HashMap<>();
            // registrationData.put("username", "user123");
            // registrationData.put("email", "user@example.com");
            // registrationData.put("password", "securePassword");
            String username = usernameField.getText();
            String fullname = fullnameField.getText();
            String address = addressField.getText();
            String email = emailField.getText();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dob = sdf.format(dobField.getDate());
            String gender = maleRadio.isSelected() ? "male" : "female";
            String password = passwordField.getText();

            User usr = new User(username, fullname, address, email, dob, gender);
            usr.setPassword(password);

            Gson gson = new Gson();
            String json = gson.toJson(usr);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            jsonObject.addProperty("header", "signup");

            return jsonObject.toString();
            // for (Map.Entry<String, String> entry : registrationData.entrySet()) {
            // String key = entry.getKey();
            // String value = entry.getValue();
            // System.out.println(key + ": " + value);
            // }
            // System.out.println(dob);
            // System.out.println(usernameField.getText());
            // System.out.println(passwordField.getText());
            // System.out.println(fullnameField.getText());
            // System.out.println(emailField.getText());
            // System.out.println(addressField.getText());
            // System.out.println(maleRadio.getText());
        }
    }

    private class HomePanel extends JPanel {
        JLabel chatLabel;
        GridBagConstraints gbc;
        JPanel chatPanel;
        JPanel chatMessagePanel;
        JPanel friendListPanel;
        String curPeer;

        HomePanel() {
            super();
            setLayout(new GridBagLayout());
            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            JPanel settingPanel = createSettingPanel();
            gbc.gridx = 0;
            gbc.weightx = 1;
            // gbc.gridwidth = 1;
            gbc.weighty = 1;
            add(settingPanel, gbc);
            // add(Box.createRigidArea(new Dimension(10, 30)));

            JPanel friendsPanel = createFriendPanel();
            gbc.gridx = 1;
            // gbc.weightx = 6;
            gbc.weightx = 8;
            // gbc.gridwidth = 6;
            gbc.weighty = 1;
            add(friendsPanel, gbc);

            user.addMessageListener(this::addMessages);

            chatPanel = createChatPanel();
            // chatPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

            gbc.gridx = 2;
            // gbc.gridwidth = 18;

            gbc.weightx = 20;
            gbc.weighty = 1;
            // chatPanel.add(chatLabel);
            // chatPanel.add(chatScrollPanel);

            // chatPanel.setVisible(false);
            add(chatPanel, gbc);

            // settingPanel.add(setting);
            // friendsPanel.add(users);
            // chatPanel.add(chat);

            // add(usersPanel);
            // add(chatPanel);

        }

        JPanel createSettingPanel() {
            JPanel settingPanel = new JPanel();
            settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
            settingPanel.setSize(120, 800);

            settingPanel.setBackground(Color.decode(dotenv.get("btnBackground")));

            JButton addFriendIcon = new JButton(new ImageIcon(imagePath + dotenv.get("img.addFriend")));
            addFriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            addFriendIcon.setToolTipText("Find friends");
            addFriendIcon.setBackground(Color.decode(dotenv.get("btnBackground")));
            addFriendIcon.setBorderPainted(false);

            addFriendIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JDialog addFriendDialog = createAddFriendDialog();

                    addFriendDialog.setVisible(true);
                }
            });
            JButton requestIcon = new JButton(new ImageIcon(imagePath + dotenv.get("img.request")));
            requestIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            requestIcon.setBackground(Color.decode(dotenv.get("btnBackground")));
            requestIcon.setToolTipText("Friend request");
            requestIcon.setBorderPainted(false);

            requestIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JDialog requestDialog = createFriendRequestDialog();

                    requestDialog.setVisible(true);
                }
            });

            JButton unfriendIcon = new JButton(new ImageIcon(imagePath + dotenv.get("img.unFriend")));
            unfriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            unfriendIcon.setBackground(Color.decode(dotenv.get("btnBackground")));
            unfriendIcon.setToolTipText("Unfriend & Block");
            unfriendIcon.setBorderPainted(false);

            JButton userIcon = new JButton(new ImageIcon(imagePath + dotenv.get("img.user")));
            userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            userIcon.setToolTipText("Profile");
            userIcon.setBackground(Color.decode(dotenv.get("btnBackground")));
            userIcon.setBorderPainted(false);

            userIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    JDialog userInfoDialog = createUserInfoDialog();

                    userInfoDialog.setVisible(true);

                }
            });

            JButton logoutIcon = new JButton(new ImageIcon(imagePath + dotenv.get("img.logout")));
            logoutIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoutIcon.setToolTipText("Log out");
            logoutIcon.setBackground(Color.decode(dotenv.get("btnBackground")));
            logoutIcon.setBorderPainted(false);

            logoutIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLO.show(panel, "LOGIN");
                    mode = Mode.LOGIN;
                    sendOffline();
                }
            });

            settingPanel.add(userIcon);
            settingPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            settingPanel.add(addFriendIcon);
            settingPanel.add(requestIcon);
            settingPanel.add(unfriendIcon);
            settingPanel.add(Box.createVerticalGlue());

            settingPanel.add(logoutIcon);
            return settingPanel;
        }

        JPanel createFriendPanel() {
            JPanel friendsPanel = new JPanel();
            friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
            friendsPanel.setBackground(Color.decode(dotenv.get("background")));

            // friendsPanel.setPreferredSize(new Dimension(400, ));
            // friendsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            // Integer.MAX_VALUE));
            friendsPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
            friendsPanel.setPreferredSize(new Dimension(400, Integer.MAX_VALUE));

            JLabel friendsLabel = new JLabel("Friend List");
            friendsLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            friendsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            friendsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, friendsLabel.getPreferredSize().height));

            ArrayList<User> friendList = user.getFriends();
            friendListPanel = new JPanel();
            friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));
            JScrollPane friendListScroll = new JScrollPane(friendListPanel);
            friendListScroll.getVerticalScrollBar().setUnitIncrement(16);
            friendListPanel.setBackground(Color.decode(dotenv.get("background")));

            // friendsLabel.setForeground(new Color(82, 82, 82));
            renderFriendList(friendList, friendListPanel);

            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
            searchPanel.setMaximumSize(new Dimension(400, 30));

            JTextField searchField = new JTextField(20);
            searchField.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchPanel.add(searchField);

            JButton searchButton = new JButton("Search");
            searchButton.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchButton.setBackground(Color.decode(dotenv.get("btnBackground")));
            searchButton.setForeground(Color.WHITE);

            searchButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ArrayList<User> filteredList = filterFriends(friendList, searchField.getText());

                    renderFriendList(filteredList, friendListPanel);

                }
            });
            searchPanel.add(searchButton);

            // JScrollPane friendListScroll = createFriendListPanel();

            friendsPanel.add(friendsLabel);
            friendsPanel.add(searchPanel);
            friendsPanel.add(friendListScroll);
            return friendsPanel;

        }

        void renderFriendList(ArrayList<User> friendList, JPanel friendListPanel) {
            friendListPanel.removeAll();
            friendList.forEach((User usr) -> {

                JButton btn = new JButton();
                JLabel textLabel = new JLabel(
                        "<html><b>" + usr.getUsername() + "</b><br/>" + usr.getFullname() + "</html>");
                textLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 18));

                String path = usr.isOnline() ? dotenv.get("img.online") : dotenv.get("img.offline");
                JLabel iconLabel = new JLabel(new ImageIcon(imagePath + path));
                // btn.setHorizontalTextPosition(SwingConstants.LEFT);
                // btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setLayout(new BorderLayout());
                btn.add(textLabel, BorderLayout.WEST);
                btn.add(iconLabel, BorderLayout.EAST);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setPreferredSize(new Dimension(300, 100));
                btn.setMaximumSize(new Dimension(300, 100));
                btn.setBackground(Color.WHITE);
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        if (me.getButton() == MouseEvent.BUTTON1) {
                            chatLabel.setText(usr.getUsername() + " - Chatting");
                            // chatPanel.setMaximumSize(new Dimension(500,
                            // chatLabel.getPreferredSize().height));

                            // chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                            new Thread(() -> {
                                SocketController sc = new SocketController();
                                String packet = createMessageReq(user.getUsername(), usr.getUsername());
                                System.out.println(packet);
                                sc.sendRequest(packet);

                                String res = sc.getResponse();
                                System.out.println("msg" + res);
                                Gson gson = new Gson();
                                Type messageListType = new TypeToken<ArrayList<ChatMessage>>() {
                                }.getType();

                                // Parse the JSON into an ArrayList of Message objects
                                ArrayList<ChatMessage> messages = gson.fromJson(res, messageListType);
                                // usr.setMessages(messages);

                                messages.forEach((ChatMessage m) -> {
                                    System.out.println(m.getContent());
                                });

                                user.setMessages(messages);
                                sc.close();

                                addMessages();
                                curPeer = usr.getUsername();
                                // chatMessagePanel.revalidate();
                                // chatMessagePanel.repaint();

                            }).start();
                        }

                    }
                });
                friendListPanel.add(btn);
            });

            friendListPanel.revalidate();
            friendListPanel.repaint();

        }

        void renderFriendRequestList(ArrayList<User> usrList, JPanel usrListPanel) {
            usrListPanel.removeAll();
            usrList.forEach((User usr) -> {

                JLabel lb = new JLabel("<html>" + usr.getUsername() + "</html>");
                lb.setFont(new Font("Nunito Sans", Font.BOLD, 22));
                lb.setAlignmentX(Component.CENTER_ALIGNMENT);
                // lb.setMaximumSize(new Dimension(130, 40));
                // btn.setPreferredSize(new Dimension(400, 100));

                JLabel messageHolder = new JLabel("");
                messageHolder.setFont(new Font("Nunito Sans", Font.PLAIN, 20));

                JButton acceptBtn = new JButton("Accept");
                acceptBtn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
                acceptBtn.setMaximumSize(new Dimension(130, 40));
                acceptBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                acceptBtn.setForeground(Color.WHITE);
                acceptBtn.setBackground(Color.decode(dotenv.get("btnBackground")));

                // messageHolder.setAlignmentX(Component.CENTER_ALIGNMENT);

                JButton rejectBtn = new JButton("Reject");
                rejectBtn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
                rejectBtn.setMaximumSize(new Dimension(130, 40));
                rejectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                rejectBtn.setForeground(Color.WHITE);
                rejectBtn.setBackground(Color.decode(dotenv.get("btnBackground")));

                JPanel userRow = new JPanel();
                userRow.setLayout(new BoxLayout(userRow, BoxLayout.X_AXIS));
                userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
                userRow.setMaximumSize(new Dimension(400, 80));

                userRow.add(lb);
                userRow.add(Box.createHorizontalStrut(10));
                userRow.add(messageHolder);
                userRow.add(acceptBtn);
                userRow.add(rejectBtn);

                userRow.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

                userRow.setBackground(Color.WHITE);

                usrListPanel.add(userRow);

                acceptBtn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "acceptrequest");
                        jsonObject.addProperty("username1", user.getUsername());
                        jsonObject.addProperty("username2", usr.getUsername());
                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();

                        if (resHeader.equals("acceptrequested")) {
                            usrListPanel.remove(userRow);
                            usrListPanel.revalidate();
                            usrListPanel.repaint();

                            JsonArray friendsArray = resObject.getAsJsonArray("friends");
                            System.out.print(friendsArray);

                            ArrayList<User> userList = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < friendsArray.size(); i++) {

                                User u = gson.fromJson(friendsArray.get(i), User.class);
                                System.out.println(u.getUsername());
                                userList.add(u);

                            }

                            user.setFriends(userList);
                            renderFriendList(userList, friendListPanel);
                        }
                    }
                });

                rejectBtn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "rejectrequest");
                        jsonObject.addProperty("username1", user.getUsername());
                        jsonObject.addProperty("username2", usr.getUsername());
                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();

                        if (resHeader.equals("rejectrequested")) {
                            usrListPanel.remove(userRow);
                            usrListPanel.revalidate();
                            usrListPanel.repaint();

                        }
                    }
                });

            });

            usrListPanel.revalidate();
            usrListPanel.repaint();

        }

        void renderFoundUserList(ArrayList<User> usrList, JPanel usrListPanel) {
            usrListPanel.removeAll();
            usrList.forEach((User usr) -> {

                JLabel lb = new JLabel(
                        "<html><b>" + usr.getUsername() + "</b><br/>" + usr.getFullname() + "</html>");
                ;
                lb.setFont(new Font("Nunito Sans", Font.BOLD, 22));
                lb.setAlignmentX(Component.CENTER_ALIGNMENT);
                // lb.setMaximumSize(new Dimension(130, 40));
                // btn.setPreferredSize(new Dimension(400, 100));

                JLabel messageHolder = new JLabel("");
                messageHolder.setFont(new Font("Nunito Sans", Font.PLAIN, 20));

                JButton btnAddFriend = new JButton("Send request");
                btnAddFriend.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
                btnAddFriend.setMaximumSize(new Dimension(130, 40));
                btnAddFriend.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnAddFriend.setForeground(Color.WHITE);
                btnAddFriend.setBackground(Color.decode(dotenv.get("btnBackground")));

                btnAddFriend.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "addfriend");
                        jsonObject.addProperty("from", user.getUsername());
                        jsonObject.addProperty("to", usr.getUsername());
                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();

                        if (resHeader.equals("addfriended")) {
                            messageHolder.setText("Sent");
                            messageHolder.setForeground(randomColor());
                        }
                    }
                });

                // messageHolder.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel userRow = new JPanel();
                userRow.setLayout(new BoxLayout(userRow, BoxLayout.X_AXIS));
                userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
                userRow.setMaximumSize(new Dimension(400, 80));

                userRow.add(lb);
                userRow.add(Box.createHorizontalStrut(10));
                userRow.add(messageHolder);
                userRow.add(btnAddFriend);

                userRow.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

                userRow.setBackground(Color.WHITE);

                usrListPanel.add(userRow);
            });

            usrListPanel.revalidate();
            usrListPanel.repaint();

        }

        ArrayList<User> filterFriends(ArrayList<User> friends, String query) {
            if (query.equals(""))
                return friends;
            ArrayList<User> resultList = new ArrayList<>();

            for (User user : friends) {
                // Kiểm tra nếu username hoặc fullname chứa query (case-insensitive)
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getFullname().toLowerCase().contains(query.toLowerCase())) {
                    resultList.add(user);
                }
            }

            return resultList;
        }

        String createMessageReq(String username1, String username2) {
            HashMap<String, String> packet = new HashMap<>();

            packet.put("header", "messages");
            packet.put("username1", username1);
            packet.put("username2", username2);

            Gson gson = new Gson();
            String json = gson.toJson(packet);
            return json;

        }

        // String createMessage(String username1, String username2) {
        // HashMap<String, String> packet = new HashMap<>();

        // packet.put("header", "messages");
        // packet.put("username1", username1);
        // packet.put("username2", username2);

        // Gson gson = new Gson();
        // String json = gson.toJson(packet);
        // return json;

        // }

        JPanel createChatPanel() {
            JPanel pan = new JPanel();

            JPanel infoPanel = new JPanel(new BorderLayout());

            infoPanel.setMaximumSize(new Dimension(800, 50));
            infoPanel.setBackground(Color.decode(dotenv.get("background")));
            // infoPanel.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));

            pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
            pan.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            pan.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // pan.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

            chatLabel = new JLabel("... - Chatting");
            chatLabel.setMaximumSize(new Dimension(800, chatLabel.getPreferredSize().height));
            // chatLabel.setPreferredSize(new Dimension(300,
            // chatLabel.getPreferredSize().height));
            // chatLabel.setPreferredSize(new Dimension(400, 100));
            chatLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // pan.setBackground(Color.YELLOW);

            chatMessagePanel = new JPanel();
            chatMessagePanel.setLayout(new BoxLayout(chatMessagePanel, BoxLayout.Y_AXIS));
            // chatMessagePanel.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
            chatMessagePanel.setBackground(Color.WHITE);
            // chatMessagePanel.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // chatMessagePanel.setPreferredSize(new Dimension(800, 100));
            JScrollPane chatMessageScroll = new JScrollPane(chatMessagePanel);
            chatMessageScroll.getVerticalScrollBar().setUnitIncrement(16);
            chatMessageScroll.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
            // chatMessageScroll.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // chatMessageScroll.setPreferredSize(new Dimension(600, 600));

            JButton infoBtn = new JButton();
            ImageIcon icon = new ImageIcon(imagePath + dotenv.get("img.dots"));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            // btn.setIconTextGap(500);
            infoBtn.setIcon(icon);
            infoBtn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            infoBtn.setBackground(Color.decode(dotenv.get("btnBackground")));
            infoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoBtn.setPreferredSize(new Dimension(100, 30));
            infoBtn.setMaximumSize(new Dimension(100, 30));
            infoBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {

                    JDialog infoDialog = createChatInfoDialog();

                    infoDialog.setVisible(true);
                }
            });
            // public void mouseClicked(MouseEvent me) {

            // HashMap<String, String> packet = new HashMap<>();
            // packet.put("header", "chat");
            // packet.put("from", user.getUsername());
            // packet.put("to", curPeer);
            // packet.put("content", textArea.getText());

            // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // LocalDateTime now = LocalDateTime.now();

            // packet.put("time_stamp", dtf.format(now));
            // packet.put("header", "chat");
            // ChatMessage msg = new ChatMessage(user.getUsername(), curPeer,
            // textArea.getText(), dtf.format(now));
            // Gson gson = new Gson();
            // String json = gson.toJson(packet);
            // System.out.println(json);
            // chatPW.println(json);
            // SocketController sc = new SocketController();
            // sc.sendRequest(json);

            // addMessage(msg);
            // sc.close();
            // textArea.setText("");
            // }

            // });
            infoPanel.add(chatLabel, BorderLayout.WEST);
            infoPanel.add(infoBtn, BorderLayout.EAST);
            pan.add(infoPanel);

            pan.add(chatMessageScroll);

            JPanel chatAreaPanel = createChatArea();
            chatAreaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            // chatAreaPanel.setPreferredSize(new Dimension(800, 100));

            pan.add(chatAreaPanel);

            pan.setBackground(Color.decode(dotenv.get("background")));

            return pan;
        }

        void addMessages() {
            chatMessagePanel.removeAll();

            ArrayList<ChatMessage> messages = user.getMessages();

            messages.forEach((ChatMessage msg) -> {
                JTextArea msgL = new JTextArea(msg.getContent());
                msgL.setFont(new Font("Nunito Sans", Font.BOLD, 19));
                msgL.setLineWrap(true); // Enable line wrapping
                msgL.setWrapStyleWord(true); // Wrap whole words

                msgL.setPreferredSize(new Dimension(400, msgL.getPreferredSize().height));

                msgL.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
                msgL.setEditable(false);
                // JLabel msgL = new JLabel("<html>" + msg.getContent() + "</html>");

                // msgL.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
                // msgL.setPreferredSize(new Dimension(400, msgL.getPreferredSize().height));
                // msgL.setPreferredSize(new Dimension(400, 80));
                // msgL.getPreferredSize().height));
                JPanel mRow = new JPanel();
                mRow.setLayout(new BorderLayout());

                mRow.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
                mRow.setBackground(Color.WHITE);

                mRow.setAlignmentX(Component.RIGHT_ALIGNMENT);
                // mRow.setPreferredSize(new Dimension(700, 50));

                // mRow.setPreferredSize(new Dimension(690, ));

                // mRow.setPreferredSize(new Dimension(200, 100));

                if (msg.getFrom().equals(user.getUsername())) {
                    // mRow.add(Box.createHorizontalGlue());
                    msgL.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    // msgL.setHorizontalAlignment(SwingConstants.RIGHT);

                    // chatMessagePanel.add(Box.createHorizontalGlue());
                    mRow.add(msgL, BorderLayout.EAST);
                } else {
                    msgL.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    mRow.add(msgL, BorderLayout.WEST);

                    // mRow.add(Box.createHorizontalGlue());
                    // mRow.add(Box.createRigidArea(new Dimension(0, 30)));

                }
                chatMessagePanel.add(mRow);
            });
            chatMessagePanel.revalidate();
            chatMessagePanel.repaint();
        }

        void addMessage(ChatMessage msg) {
            user.addMessage(msg);
            addMessages();
            // chatMessagePanel.revalidate();
            // chatMessagePanel.repaint();

        }

        JDialog createUserInfoDialog() {
            JDialog dialog = new JDialog(frame, "User info");
            JPanel dPanel = new JPanel();
            dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.Y_AXIS));

            JLabel usernameLabel = new JLabel("Username");
            usernameLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            // usernameLabel.setAlignmentX(Component.CEN);

            JLabel usnField = new JLabel(user.getUsername());
            usnField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            // usnField.setMaximumSize(new);

            JLabel fullnameLabel = new JLabel("Full name");
            fullnameLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            JTextField fnField = new JTextField(40);
            fnField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            fnField.setText(user.getFullname());

            fnField.setMaximumSize(fnField.getPreferredSize());
            // fnField.setMaximumSize(fnField);

            JLabel addressLabel = new JLabel("Address");
            addressLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            JTextField addrField = new JTextField(40);
            addrField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            addrField.setText(user.getAddress());

            addrField.setMaximumSize(addrField.getPreferredSize());

            JLabel dobLabel = new JLabel("Date of Birth");
            dobLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            // JTextField dField = new JTextField(40);
            // dField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            // dField.setText(user.getDob());

            // dField.setMaximumSize(dField.getPreferredSize());

            JDateChooser dField = new JDateChooser();
            dField.setMaximumSize(new Dimension(800, 30));
            dField.setPreferredSize(new Dimension(800, 30));

            dField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                dField.setDate(sdf.parse(user.getDob()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JLabel genderLabel = new JLabel("Gender");
            genderLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            // JTextField gField = new JTextField(40);
            // gField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            // gField.setText(user.getGender());

            // gField.setMaximumSize(gField.getPreferredSize());

            JRadioButton mRadio = new JRadioButton("Male");
            JRadioButton fRadio = new JRadioButton("Female");
            mRadio.setBackground(Color.decode(dotenv.get("background")));
            fRadio.setBackground(Color.decode(dotenv.get("background")));

            mRadio.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            mRadio.setSelected(user.getGender().equals("male"));
            fRadio.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            fRadio.setSelected(user.getGender().equals("female"));

            ButtonGroup bg = new ButtonGroup();
            bg.add(mRadio);
            bg.add(fRadio);
            JPanel genderRow = new JPanel();
            genderRow.add(mRadio);
            genderRow.add(fRadio);
            genderRow.setMaximumSize(genderRow.getPreferredSize());
            genderRow.setBackground(Color.decode(dotenv.get("background")));
            // genderRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel emailLabel = new JLabel("Email");
            emailLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            JTextField emField = new JTextField(40);
            emField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            emField.setText(user.getEmail());

            emField.setMaximumSize(emField.getPreferredSize());

            JLabel curPasswordLabel = new JLabel("Current password");
            curPasswordLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            JTextField curPasswordField = new JTextField(40);
            curPasswordField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            // curPasswordField.setText(user.getEmail());
            curPasswordField.setMaximumSize(emField.getPreferredSize());

            JLabel newPasswordLabel = new JLabel("New password");
            newPasswordLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));

            JTextField newPasswordField = new JTextField(40);
            newPasswordField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            // newPasswordField.setText(user.getEmail());

            newPasswordField.setMaximumSize(emField.getPreferredSize());

            JLabel messageHolder = new JLabel("");
            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 18));

            JButton saveButton = new JButton("SAVE CHANGE");
            saveButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            saveButton.setForeground(Color.WHITE);
            saveButton.setBackground(Color.decode(dotenv.get("btnBackground")));

            saveButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    boolean flag = true;

                    HashMap<String, String> editData = new HashMap<>();
                    String username = usnField.getText();
                    String fullname = fnField.getText();
                    String address = addrField.getText();
                    String email = emField.getText();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dob = sdf.format(dField.getDate());
                    String gender = mRadio.isSelected() ? "male" : "female";
                    String curPassword = curPasswordField.getText();
                    String newPassword = newPasswordField.getText();

                    editData.put("username", username);
                    editData.put("fullname", fullname);
                    editData.put("address", address);
                    editData.put("email", email);
                    editData.put("dob", dob);
                    editData.put("gender", gender);

                    for (Map.Entry<String, String> entry : editData.entrySet()) {
                        if (entry.getValue().equals("")) {
                            messageHolder.setText("Empty fields are not allowed");
                            messageHolder.setForeground(Color.RED);
                            flag = false;
                            break;
                        }
                    }

                    if (!curPassword.equals("")) {
                        if (newPassword.equals("")) {
                            messageHolder.setText("<html>" + messageHolder.getText()
                                    + "<br/>Empty new password are not allowed<html/>");
                            messageHolder.setForeground(Color.RED);
                            flag = false;

                        }

                    }
                    System.out.println("pw" + user.getPassword());

                    if (curPassword.equals("") && newPassword.equals("")) {
                        curPassword = newPassword = user.getPassword();
                    }
                    System.out.println("cur" + curPassword.equals(""));

                    editData.put("password", curPassword);
                    editData.put("newPassword", newPassword);
                    System.out.println("cur" + curPassword.equals(""));

                    if (flag) {
                        Gson gson = new Gson();
                        String json = gson.toJson(editData);
                        System.out.println("js" + json + "cur: " + curPassword);
                        System.out.println("cur" + curPassword.equals(""));

                        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                        jsonObject.addProperty("header", "edit");

                        SocketController sc = new SocketController();
                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        jsonObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = jsonObject.get("header").getAsString();
                        if (resHeader.equals("edited")) {
                            // System.out.println("Break1");
                            String pass = newPassword;
                            System.out.println("new" + pass);
                            user = gson.fromJson(res, User.class);
                            user.setPassword(pass);

                            messageHolder.setText("EDITED SUCCESSFULLY");
                            messageHolder.setForeground(randomColor());

                            // System.out.println("Break2");
                            revalidate();
                            repaint();

                        } else {
                            messageHolder.setText("EDITED FAILED");
                            messageHolder.setForeground(randomColor());

                        }

                    }

                    // return jsonObject.toString();
                }
            });

            // messageHolder.setForeground(Color.decode("#ffc107"));

            dialog.setSize(500, 700);

            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            // saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            dPanel.add(usernameLabel);
            dPanel.add(usnField);
            dPanel.add(fullnameLabel);
            dPanel.add(fnField);
            dPanel.add(addressLabel);
            dPanel.add(addrField);
            dPanel.add(dobLabel);
            dPanel.add(dField);
            dPanel.add(genderLabel);
            dPanel.add(genderRow);
            dPanel.add(emailLabel);
            dPanel.add(emField);
            dPanel.add(curPasswordLabel);
            dPanel.add(curPasswordField);
            dPanel.add(newPasswordLabel);
            dPanel.add(newPasswordField);
            dPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            dPanel.add(saveButton);
            dPanel.add(messageHolder);

            dPanel.setBackground(Color.decode(dotenv.get("background")));
            dialog.add(dPanel);

            dialog.setLocationRelativeTo(null); // center the frame

            dialog.setResizable(false);

            // System.out.println(user.getFriends().get(0));

            return dialog;

        }

        JDialog createChatInfoDialog() {

            JDialog dialog = new JDialog(frame, "Information");
            JPanel dPanel = new JPanel();
            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.Y_AXIS));

            JLabel messageHolder = new JLabel("");
            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 20));

            JButton unfriendBtn = new JButton();
            JLabel ufLabel = new JLabel(
                    "Unfriend");
            ufLabel.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            ufLabel.setForeground(Color.WHITE);

            JLabel ufIcon = new JLabel(new ImageIcon(imagePath + dotenv.get("img.unFriend")));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            unfriendBtn.setLayout(new BorderLayout());
            unfriendBtn.add(ufLabel, BorderLayout.WEST);
            unfriendBtn.add(ufIcon, BorderLayout.EAST);
            unfriendBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            unfriendBtn.setPreferredSize(new Dimension(200, 55));
            unfriendBtn.setMaximumSize(new Dimension(200, 55));
            unfriendBtn.setBackground(Color.decode(dotenv.get("btnBackground")));
            unfriendBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    SocketController sc = new SocketController();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("header", "unfriend");
                    jsonObject.addProperty("username1", user.getUsername());
                    jsonObject.addProperty("username2", curPeer);
                    sc.sendRequest(jsonObject.toString());

                    String res = sc.getResponse();
                    sc.close();

                    System.out.println(res);
                    JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                    String resHeader = resObject.get("header").getAsString();

                    if (resHeader.equals("unfriended")) {

                        JsonArray friendsArray = resObject.getAsJsonArray("friends");
                        System.out.print(friendsArray);

                        ArrayList<User> userList = new ArrayList<>();
                        Gson gson = new Gson();
                        for (int i = 0; i < friendsArray.size(); i++) {

                            User u = gson.fromJson(friendsArray.get(i), User.class);
                            System.out.println(u.getUsername());
                            userList.add(u);

                        }

                        user.setFriends(userList);
                        renderFriendList(userList, friendListPanel);

                        messageHolder.setText("Done");
                        messageHolder.setForeground(randomColor());
                    }
                }
            });
            JButton blockBtn = new JButton();
            JLabel blcLabel = new JLabel(
                    "Block");
            blcLabel.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            blcLabel.setForeground(Color.WHITE);

            JLabel blcIcon = new JLabel(new ImageIcon(imagePath + dotenv.get("img.block")));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            blockBtn.setLayout(new BorderLayout());
            blockBtn.add(blcLabel, BorderLayout.WEST);
            blockBtn.add(blcIcon, BorderLayout.EAST);
            blockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            blockBtn.setPreferredSize(new Dimension(200, 55));
            blockBtn.setMaximumSize(new Dimension(200, 55));
            blockBtn.setBackground(Color.decode(dotenv.get("btnBackground")));

            blockBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    SocketController sc = new SocketController();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("header", "block");
                    jsonObject.addProperty("username1", user.getUsername());
                    jsonObject.addProperty("username2", curPeer);
                    sc.sendRequest(jsonObject.toString());

                    String res = sc.getResponse();
                    sc.close();

                    // System.out.println(res);
                    JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                    String resHeader = resObject.get("header").getAsString();

                    if (resHeader.equals("blocked")) {

                        JsonArray friendsArray = resObject.getAsJsonArray("friends");
                        System.out.print(friendsArray);

                        ArrayList<User> userList = new ArrayList<>();
                        Gson gson = new Gson();
                        for (int i = 0; i < friendsArray.size(); i++) {

                            User u = gson.fromJson(friendsArray.get(i), User.class);
                            System.out.println(u.getUsername());
                            userList.add(u);

                        }

                        user.setFriends(userList);
                        renderFriendList(userList, friendListPanel);

                        messageHolder.setText("Done");
                        messageHolder.setForeground(randomColor());
                    }
                }
            });

            JButton spamBtn = new JButton();
            JLabel spLabel = new JLabel(
                    "Report spam");
            spLabel.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            spLabel.setForeground(Color.WHITE);

            JLabel spIcon = new JLabel(new ImageIcon(imagePath + dotenv.get("img.spam")));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            spamBtn.setLayout(new BorderLayout());
            spamBtn.add(spLabel, BorderLayout.WEST);
            spamBtn.add(spIcon, BorderLayout.EAST);
            spamBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            spamBtn.setPreferredSize(new Dimension(200, 55));
            spamBtn.setMaximumSize(new Dimension(200, 55));
            spamBtn.setBackground(Color.decode(dotenv.get("btnBackground")));

            JButton deleteChatBtn = new JButton();
            JLabel dcLabel = new JLabel(
                    "Delete chat");
            dcLabel.setFont(new Font("Nunito Sans", Font.BOLD, 18));
            dcLabel.setForeground(Color.WHITE);

            JLabel dcIcon = new JLabel(new ImageIcon(imagePath + dotenv.get("img.trash")));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            deleteChatBtn.setLayout(new BorderLayout());
            deleteChatBtn.add(dcLabel, BorderLayout.WEST);
            deleteChatBtn.add(dcIcon, BorderLayout.EAST);
            deleteChatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteChatBtn.setPreferredSize(new Dimension(200, 55));
            deleteChatBtn.setMaximumSize(new Dimension(200, 55));
            deleteChatBtn.setBackground(Color.decode(dotenv.get("btnBackground")));
            deleteChatBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    SocketController sc = new SocketController();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("header", "deletechat");
                    jsonObject.addProperty("username1", user.getUsername());
                    jsonObject.addProperty("username2", curPeer);
                    sc.sendRequest(jsonObject.toString());

                    String res = sc.getResponse();
                    sc.close();

                    // System.out.println(res);
                    JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                    String resHeader = resObject.get("header").getAsString();

                    if (resHeader.equals("deletechated")) {

                        messageHolder.setText("Done");
                        messageHolder.setForeground(randomColor());

                        user.setMessages(new ArrayList<>());

                        addMessages();
                    }
                }
            });

            dPanel.add(unfriendBtn);
            dPanel.add(Box.createVerticalGlue());
            dPanel.add(blockBtn);
            dPanel.add(Box.createVerticalGlue());
            dPanel.add(spamBtn);
            dPanel.add(Box.createVerticalGlue());
            dPanel.add(deleteChatBtn);
            dPanel.add(Box.createVerticalGlue());
            dPanel.add(messageHolder);
            dialog.setSize(250, 350);
            dialog.add(dPanel);
            dialog.setLocationRelativeTo(null);
            return dialog;
        }

        JDialog createAddFriendDialog() {
            JDialog dialog = new JDialog(frame, "Find friend");
            JPanel dPanel = new JPanel();
            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.Y_AXIS));

            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
            searchPanel.setMaximumSize(new Dimension(400, 30));

            JPanel usrListPanel = new JPanel();
            usrListPanel.setLayout(new BoxLayout(usrListPanel, BoxLayout.Y_AXIS));
            usrListPanel.setBackground(Color.decode(dotenv.get("background")));

            // usrListPanel.setMaximumSize(new Dimension(3, 30));
            JScrollPane usrListScroll = new JScrollPane(usrListPanel);
            usrListScroll.getVerticalScrollBar().setUnitIncrement(10);

            JTextField searchField = new JTextField(20);
            searchField.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchPanel.add(searchField);

            JButton searchButton = new JButton("Search");
            searchButton.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchButton.setForeground(Color.WHITE);
            searchButton.setBackground(Color.decode(dotenv.get("btnBackground")));
            searchButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!searchField.getText().equals("")) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "findfriend");
                        jsonObject.addProperty("username", searchField.getText());
                        jsonObject.addProperty("from", user.getUsername());
                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();

                        if (resHeader.equals("findfriended")) {

                            JsonObject jo = JsonParser.parseString(res).getAsJsonObject();

                            JsonArray friendsArray = jo.getAsJsonArray("friends");

                            ArrayList<User> userList = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < friendsArray.size(); i++) {

                                User u = gson.fromJson(friendsArray.get(i), User.class);
                                if (!user.getFriends().contains(u) && !user.getUsername().equals(u.getUsername())) {
                                    userList.add(u);
                                }
                            }
                            System.out.println("kit");

                            renderFoundUserList(userList, usrListPanel);

                        }

                    }
                }
            });
            searchPanel.add(searchButton);
            dPanel.add(searchPanel);
            dPanel.setBackground(Color.decode(dotenv.get("background")));

            dialog.setSize(500, 700);
            dPanel.add(usrListScroll);
            dialog.add(dPanel);
            dialog.setLocationRelativeTo(null);
            return dialog;
        }

        ArrayList<User> getFriendRequest() {
            SocketController sc = new SocketController();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("header", "friendrequest");
            jsonObject.addProperty("username", user.getUsername());
            sc.sendRequest(jsonObject.toString());

            String res = sc.getResponse();

            sc.close();

            JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
            String resHeader = resObject.get("header").getAsString();

            ArrayList<User> userList = new ArrayList<>();
            if (resHeader.equals("friendrequested")) {

                JsonObject jo = JsonParser.parseString(res).getAsJsonObject();

                JsonArray friendsArray = jo.getAsJsonArray("requests");

                Gson gson = new Gson();
                for (int i = 0; i < friendsArray.size(); i++) {

                    User u = gson.fromJson(friendsArray.get(i), User.class);

                    userList.add(u);

                }

            }
            return userList;

        }

        JDialog createFriendRequestDialog() {
            ArrayList<User> userList = getFriendRequest();
            JDialog dialog = new JDialog(frame, "Friend request");
            JPanel dPanel = new JPanel();
            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.Y_AXIS));

            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
            searchPanel.setMaximumSize(new Dimension(400, 30));

            JPanel usrListPanel = new JPanel();
            usrListPanel.setLayout(new BoxLayout(usrListPanel, BoxLayout.Y_AXIS));
            usrListPanel.setBackground(Color.decode(dotenv.get("background")));

            // usrListPanel.setMaximumSize(new Dimension(3, 30));
            JScrollPane usrListScroll = new JScrollPane(usrListPanel);
            usrListScroll.getVerticalScrollBar().setUnitIncrement(10);

            JTextField searchField = new JTextField(20);
            searchField.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchPanel.add(searchField);

            renderFriendRequestList(userList, usrListPanel);

            JButton searchButton = new JButton("Search");
            searchButton.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchButton.setForeground(Color.WHITE);
            searchButton.setBackground(Color.decode(dotenv.get("btnBackground")));
            searchButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    ArrayList<User> filteredUsers = new ArrayList<>();
                    System.out.println("test");
                    System.out.println(searchField.getText());

                    if (searchField.getText().equals("")) {
                        System.out.println("empty");
                        filteredUsers = userList;
                    } else {
                        for (User usr : userList) {
                            if (usr.getUsername().toLowerCase().contains(searchField.getText().toLowerCase())) {
                                filteredUsers.add(usr);
                            }
                        }
                    }
                    renderFriendRequestList(filteredUsers, usrListPanel);
                }
            });
            searchPanel.add(searchButton);
            dPanel.add(searchPanel);
            dPanel.setBackground(Color.decode(dotenv.get("background")));

            dialog.setSize(500, 700);
            dPanel.add(usrListScroll);
            dialog.add(dPanel);
            dialog.setLocationRelativeTo(null);
            return dialog;
        }

        JPanel createChatArea() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            JTextArea textArea = new JTextArea("Chat Here", 5, 20);
            textArea.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            // textArea.setPreferredSize(new Dimension(800, 100));
            textArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            // textArea.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 100));
            scrollPane.setPreferredSize(new Dimension(800, 100));

            // scrollPane.setMaximumSize(new Dimension(800, 100));
            // scrollPane.setMaximumSize(new Dimension(800, 100));

            JButton btn = new JButton();
            ImageIcon icon = new ImageIcon(imagePath + dotenv.get("img.send"));
            // btn.setHorizontalTextPosition(SwingConstants.LEFT);
            // btn.setHorizontalAlignment(SwingConstants.LEFT);
            // btn.setIconTextGap(500);
            btn.setIcon(icon);
            btn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            btn.setBackground(Color.decode(dotenv.get("btnBackground")));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setPreferredSize(new Dimension(100, 100));
            btn.setMaximumSize(new Dimension(100, 100));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {

                    HashMap<String, String> packet = new HashMap<>();
                    packet.put("header", "chat");
                    packet.put("from", user.getUsername());
                    packet.put("to", curPeer);
                    packet.put("content", textArea.getText());

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    packet.put("time_stamp", dtf.format(now));
                    packet.put("header", "chat");
                    ChatMessage msg = new ChatMessage(user.getUsername(), curPeer, textArea.getText(), dtf.format(now));
                    Gson gson = new Gson();
                    String json = gson.toJson(packet);
                    System.out.println(json);
                    chatPW.println(json);
                    SocketController sc = new SocketController();
                    sc.sendRequest(json);

                    addMessage(msg);
                    sc.close();
                    textArea.setText("");
                }

            });

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(btn, BorderLayout.WEST);

            // panel.setVisible(false);

            return panel;
        }

    }

    static Color randomColor() {
        // Tạo đối tượng Random
        Random rand = new Random();

        int red = rand.nextInt(256);
        int green = rand.nextInt(256);
        int blue = rand.nextInt(256);

        return new Color(red, green, blue);
    }
}
