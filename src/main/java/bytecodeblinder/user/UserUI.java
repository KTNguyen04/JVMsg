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
import bytecodeblinder.config.AppConfig;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class UserView {
    private JFrame frame;
    private JPanel panel;
    private CardLayout cardLO;
    private PrintWriter chatPW;

    private Mode mode;

    static User user;

    private enum Mode {
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

        // sc = null;

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

            BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath + "banner.png"));
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

            BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath + "banner.png"));
            Image scaledImage = bannerImage.getScaledInstance(AppConfig.bannerWidth, AppConfig.bannerHeight,
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
            JPanel friendsPanel = createFriendPanel();
            gbc.gridx = 1;
            // gbc.weightx = 6;
            gbc.weightx = 6;
            // gbc.gridwidth = 6;
            gbc.weighty = 1;
            add(friendsPanel, gbc);

            user.addMessageListener(this::addMessages);

            chatPanel = createChatPanel();
            // chatPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

            gbc.gridx = 2;
            // gbc.gridwidth = 18;

            gbc.weightx = 18;
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

            settingPanel.setBackground(Color.WHITE);

            JLabel addFriendIcon = new JLabel(new ImageIcon(AppConfig.bannerPath + "add_friend.png"));
            addFriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            addFriendIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JDialog addFriendDialog = createAddFriendDialog();

                    addFriendDialog.setVisible(true);
                }
            });
            JLabel friendsIcon = new JLabel(new ImageIcon(AppConfig.bannerPath + "friends.png"));
            friendsIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel unfriendIcon = new JLabel(new ImageIcon(AppConfig.bannerPath + "unfriend.png"));
            unfriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel userIcon = new JLabel(new ImageIcon(AppConfig.bannerPath + "user.png"));
            userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            userIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    JDialog userInfoDialog = createUserInfoDialog();

                    userInfoDialog.setVisible(true);

                }
            });

            JLabel logoutIcon = new JLabel(new ImageIcon(AppConfig.bannerPath + "logout.png"));
            logoutIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoutIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLO.show(panel, "LOGIN");
                    mode = Mode.LOGIN;
                    sendOffline();
                }
            });

            settingPanel.add(userIcon);
            settingPanel.add(addFriendIcon);
            settingPanel.add(friendsIcon);
            settingPanel.add(unfriendIcon);
            settingPanel.add(Box.createVerticalGlue());
            settingPanel.add(logoutIcon);
            return settingPanel;
        }

        JPanel createFriendPanel() {
            JPanel friendsPanel = new JPanel();
            friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
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
            JPanel friendListPanel = new JPanel();
            friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));
            JScrollPane friendListScroll = new JScrollPane(friendListPanel);
            friendListScroll.getVerticalScrollBar().setUnitIncrement(16);

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

                JButton btn = new JButton(
                        "<html><b>" + usr.getUsername() + "</b><br/>" + usr.getFullname() + "</html>");
                String path = usr.isOnline() ? "online.png" : "offline.png";
                ImageIcon icon = new ImageIcon(AppConfig.bannerPath + path);
                btn.setHorizontalTextPosition(SwingConstants.LEFT);
                // btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setIcon(icon);
                btn.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                // btn.setPreferredSize(new Dimension(400, 100));
                btn.setMaximumSize(new Dimension(400, 80));
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

        void renderFoundUserList(ArrayList<User> usrList, JPanel usrListPanel) {
            usrListPanel.removeAll();
            usrList.forEach((User usr) -> {

                JLabel lb = new JLabel(
                        "<html><b>" + usr.getUsername() + "</b><br/>" + usr.getFullname() + "</html>");
                ;
                lb.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
                lb.setAlignmentX(Component.CENTER_ALIGNMENT);
                // btn.setPreferredSize(new Dimension(400, 100));

                JButton btnAddFriend = new JButton("Add");
                btnAddFriend.setFont(new Font("Nunito Sans", Font.PLAIN, 14));
                btnAddFriend.setMaximumSize(new Dimension(150, 40));
                btnAddFriend.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnAddFriend.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "addfriend");
                        jsonObject.addProperty("from", user.getUsername());
                        jsonObject.addProperty("to", usr.getUsername());
                        sc.sendRequest(jsonObject.toString());

                        // String res = sc.getResponse();
                        sc.close();

                        // System.out.println(res);
                        // JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        // String resHeader = resObject.get("header").getAsString();

                        // if (resHeader.equals("findfriended")) {

                        // JsonObject jo = JsonParser.parseString(res).getAsJsonObject();

                        // JsonArray friendsArray = jo.getAsJsonArray("friends");

                        // ArrayList<User> userList = new ArrayList<>();
                        // Gson gson = new Gson();
                        // for (int i = 0; i < friendsArray.size(); i++) {
                        // // Chuyển mỗi phần tử trong JsonArray thành đối tượng User
                        // User user = gson.fromJson(friendsArray.get(i), User.class);
                        // userList.add(user);
                        // }
                        // System.out.println("kit");

                        // renderFoundUserList(userList, usrListPanel);

                        // }
                    }
                });

                JPanel userRow = new JPanel();
                userRow.setLayout(new BoxLayout(userRow, BoxLayout.X_AXIS));
                userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
                userRow.setMaximumSize(new Dimension(400, 80));

                userRow.add(lb);
                userRow.add(Box.createHorizontalStrut(10));
                userRow.add(btnAddFriend);

                userRow.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

                userRow.setBackground(Color.lightGray); // M

                // btn.addMouseListener(new MouseAdapter() {
                // public void mouseClicked(MouseEvent me) {
                // if (me.getButton() == MouseEvent.BUTTON1) {
                // chatLabel.setText(usr.getUsername() + " - Chatting");
                // // chatPanel.setMaximumSize(new Dimension(500,
                // // chatLabel.getPreferredSize().height));

                // // chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                // new Thread(() -> {
                // SocketController sc = new SocketController();
                // String packet = createMessageReq(user.getUsername(), usr.getUsername());
                // System.out.println(packet);
                // sc.sendRequest(packet);

                // String res = sc.getResponse();
                // System.out.println("msg" + res);
                // Gson gson = new Gson();
                // Type messageListType = new TypeToken<ArrayList<ChatMessage>>() {
                // }.getType();

                // // Parse the JSON into an ArrayList of Message objects
                // ArrayList<ChatMessage> messages = gson.fromJson(res, messageListType);
                // // usr.setMessages(messages);

                // messages.forEach((ChatMessage m) -> {
                // System.out.println(m.getContent());
                // });

                // user.setMessages(messages);
                // sc.close();

                // addMessages();
                // curPeer = usr.getUsername();
                // // chatMessagePanel.revalidate();
                // // chatMessagePanel.repaint();

                // }).start();
                // }

                // }
                // });
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
            JPanel chatPanel = new JPanel();
            chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
            chatPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            chatPanel.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // chatPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

            chatLabel = new JLabel("... - Chatting");
            chatLabel.setMaximumSize(new Dimension(800, chatLabel.getPreferredSize().height));
            // chatLabel.setPreferredSize(new Dimension(300,
            // chatLabel.getPreferredSize().height));
            // chatLabel.setPreferredSize(new Dimension(400, 100));
            chatLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // chatPanel.setBackground(Color.YELLOW);

            chatMessagePanel = new JPanel();
            chatMessagePanel.setLayout(new BoxLayout(chatMessagePanel, BoxLayout.Y_AXIS));
            chatMessagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // chatMessagePanel.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // chatMessagePanel.setPreferredSize(new Dimension(800, 100));
            JScrollPane chatMessageScroll = new JScrollPane(chatMessagePanel);
            chatMessageScroll.getVerticalScrollBar().setUnitIncrement(16);
            chatMessageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // chatMessageScroll.setPreferredSize(new Dimension(800, Integer.MAX_VALUE));
            // chatMessageScroll.setPreferredSize(new Dimension(600, 600));

            chatPanel.add(chatLabel);

            chatPanel.add(chatMessageScroll);

            JPanel chatAreaPanel = createChatArea();
            chatAreaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            // chatAreaPanel.setPreferredSize(new Dimension(800, 100));

            chatPanel.add(chatAreaPanel);

            return chatPanel;
        }

        void addMessages() {
            chatMessagePanel.removeAll();

            ArrayList<ChatMessage> messages = user.getMessages();

            messages.forEach((ChatMessage msg) -> {
                JLabel msgL = new JLabel(msg.getContent());
                msgL.setFont(new Font("Nunito Sans", Font.PLAIN, 16));

                // msgL.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                // msgL.getPreferredSize().height));
                JPanel mRow = new JPanel();
                mRow.setLayout(new BoxLayout(mRow, BoxLayout.X_AXIS));
                if (msg.getFrom().equals(user.getUsername())) {
                    mRow.add(Box.createHorizontalGlue());
                    msgL.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    // chatMessagePanel.add(Box.createHorizontalGlue());
                    mRow.add(msgL);
                } else {
                    System.out.println("123");

                    msgL.setAlignmentX(Component.LEFT_ALIGNMENT);
                    mRow.add(msgL);
                    mRow.add(Box.createHorizontalGlue());
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
            // usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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

            JLabel messageHolder = new JLabel("test");
            messageHolder.setFont(new Font("Nunito Sans", Font.BOLD, 18));

            JButton saveButton = new JButton("SAVE CHANGE");
            saveButton.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
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

            saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);

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

            dialog.add(dPanel);

            dialog.setLocationRelativeTo(null); // center the frame

            dialog.setResizable(false);

            // System.out.println(user.getFriends().get(0));

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
            JScrollPane usrListScroll = new JScrollPane(usrListPanel);
            usrListScroll.getVerticalScrollBar().setUnitIncrement(10);

            JTextField searchField = new JTextField(20);
            searchField.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchPanel.add(searchField);

            JButton searchButton = new JButton("Search");
            searchButton.setFont(new Font("Nunito Sans", Font.BOLD, 19));
            searchButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!searchField.getText().equals("")) {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "findfriend");
                        jsonObject.addProperty("username", searchField.getText());
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
                                // Chuyển mỗi phần tử trong JsonArray thành đối tượng User
                                User u = gson.fromJson(friendsArray.get(i), User.class);
                                if (!user.getFriends().contains(u)) {
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

            dialog.setSize(500, 700);
            dPanel.add(usrListScroll);
            dialog.add(dPanel);
            dialog.setLocationRelativeTo(null); // center the frame
            return dialog;
        }

        JPanel createChatArea() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            JTextArea textArea = new JTextArea("Chat Here", 5, 20);
            textArea.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            // textArea.setPreferredSize(new Dimension(800, 100));
            textArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            // textArea.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 100));
            scrollPane.setPreferredSize(new Dimension(800, 100));

            // scrollPane.setMaximumSize(new Dimension(800, 100));
            // scrollPane.setMaximumSize(new Dimension(800, 100));

            JButton btn = new JButton("Send");
            btn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
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
