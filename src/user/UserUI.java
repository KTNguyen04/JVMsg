package user;

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
import config.AppConfig;
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
        mode = Mode.LOGIN;

        cardLO = new CardLayout();
        panel = new JPanel(cardLO);

        panel.add("LOGIN", new LoginPanel());
        panel.add("SIGNUP", new SignupPanel());
        // panel.add("HOME", new HomePanel());

        cardLO.show(panel, "LOGIN");
        // new SignupPanel(panel);
        frame.add(panel);
        frame.setVisible(true);

        // sc = null;

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
            submitButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        String packet = getLogInData();
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

                            panel.add("HOME", new HomePanel());

                            cardLO.show(panel, "HOME");

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

        static Color randomColor() {
            // Tạo đối tượng Random
            Random rand = new Random();

            int red = rand.nextInt(256);
            int green = rand.nextInt(256);
            int blue = rand.nextInt(256);

            return new Color(red, green, blue);
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

            BufferedImage bannerImage = ImageIO.read(new File(AppConfig.bannerPath));
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

            JButton submitButton = new JButton("Sign Up");
            submitButton.setFont(new Font("Nunito Sans", Font.BOLD, 20));
            submitButton.setBackground(new Color(127, 38, 91));
            submitButton.setForeground(Color.WHITE);
            submitButton.setPreferredSize(new Dimension(380, 40));
            submitButton.setMaximumSize(new Dimension(380, 40));
            submitButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    SocketController sc = new SocketController();
                    String packet = getSignUpData();
                    sc.sendRequest(packet);
                    sc.close();
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
            rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            rightPanel.add(loginInstruction);
            rightPanel.add(Box.createVerticalGlue());

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        String getSignUpData() {
            HashMap<String, String> signupData = new HashMap<>();
            // registrationData.put("username", "user123");
            // registrationData.put("email", "user@example.com");
            // registrationData.put("password", "securePassword");
            String username = usernameField.getText();
            String fullname = fullnameField.getText();
            String address = addressField.getText();
            String email = emailField.getText();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dob = sdf.format(dobField.getDate());
            String gender = maleRadio.isSelected() ? "male" : "female";
            String password = passwordField.getText();

            signupData.put("header", "signup");
            signupData.put("username", username);
            signupData.put("fullname", fullname);
            signupData.put("address", address);
            signupData.put("email", email);
            signupData.put("dob", dob);
            signupData.put("gender", gender);
            signupData.put("password", password);

            Gson gson = new Gson();
            String json = gson.toJson(signupData);
            return json;
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
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.weighty = 1;
            add(settingPanel, gbc);
            JPanel friendsPanel = createFriendPanel();
            gbc.gridx = 1;
            // gbc.weightx = 6;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.weighty = 1;
            add(friendsPanel, gbc);

            user.addMessageListener(this::addMessages);

            // JPanel chatPanel = new JPanel();
            // chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));

            // // chatPanel.setBackground(Color.YELLOW);
            // gbc.gridx = 2;
            // gbc.weightx = 18;
            // gbc.weighty = 1;

            // chatLabel = new JLabel("Chatting");
            // // chatLabel.setMaximumSize(new Dimension(400, 100));
            // // chatLabel.setPreferredSize(new Dimension(400, 100));
            // chatLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            // chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // JScrollPane chatScrollPanel = createChatScrollPanel();
            // chatScrollPanel.setPreferredSize(new Dimension(500, 100));
            // chatScrollPanel.setMaximumSize(new Dimension(500, 600));
            chatPanel = createChatPanel();
            gbc.gridx = 2;
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

            JLabel addFriendIcon = new JLabel(new ImageIcon("./src/user/asset/imgs/add_friend.png"));
            addFriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel friendsIcon = new JLabel(new ImageIcon("./src/user/asset/imgs/friends.png"));
            friendsIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel unfriendIcon = new JLabel(new ImageIcon("./src/user/asset/imgs/unfriend.png"));
            unfriendIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel userIcon = new JLabel(new ImageIcon("./src/user/asset/imgs/user.png"));
            userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            userIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    JDialog userInfoDialog = createUserInfoDialog();

                    userInfoDialog.setVisible(true);

                }
            });

            settingPanel.add(userIcon);
            settingPanel.add(addFriendIcon);
            settingPanel.add(friendsIcon);
            settingPanel.add(unfriendIcon);
            return settingPanel;
        }

        JPanel createFriendPanel() {
            JPanel friendsPanel = new JPanel();
            friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
            // usersPanel.setBorder(BorderFactory.create(0, 20, 0, 0));

            // friendsPanel.setBackground(Color.BLUE);
            gbc.gridx = 1;
            // gbc.weightx = 6;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.weighty = 1;

            JLabel friendsLabel = new JLabel("Friend List");
            friendsLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            friendsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // JScrollPane friendListScroll = createFriendListPanel();

            JPanel friendListPanel = new JPanel();
            friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));
            JScrollPane friendListScroll = new JScrollPane(friendListPanel);
            friendListScroll.getVerticalScrollBar().setUnitIncrement(16);

            // friendsLabel.setForeground(new Color(82, 82, 82));
            ArrayList<User> friendList = user.getFriends();
            friendList.forEach((User usr) -> {
                JButton btn = new JButton(
                        "<html><b>" + usr.getUsername() + "</b><br/>" + usr.getFullname() + "</html>");
                btn.setFont(new Font("Nunito Sans", Font.PLAIN, 22));
                // btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setPreferredSize(new Dimension(400, 100));
                btn.setMaximumSize(new Dimension(400, 100));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        chatLabel.setText(usr.getUsername() + " - Chatting");
                        // chatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                        new Thread(() -> {
                            SocketController sc = new SocketController();
                            String packet = createMessageReq(user.getUsername(), usr.getUsername());
                            System.out.println(packet);
                            sc.sendRequest(packet);

                            String res = sc.getResponse();
                            System.out.println("msg" + res);
                            Gson gson = new Gson();
                            Type messageListType = new TypeToken<ArrayList<Message>>() {
                            }.getType();

                            // Parse the JSON into an ArrayList of Message objects
                            ArrayList<Message> messages = gson.fromJson(res, messageListType);
                            // usr.setMessages(messages);

                            messages.forEach((Message m) -> {
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
                });
                friendListPanel.add(btn);
            });
            friendsPanel.add(friendsLabel);
            friendsPanel.add(friendListScroll);
            return friendsPanel;

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
            chatLabel = new JLabel("Chatting");
            chatLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, chatLabel.getPreferredSize().height));
            // chatLabel.setPreferredSize(new Dimension(400, 100));
            chatLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            // chatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            // chatPanel.setBackground(Color.YELLOW);

            chatMessagePanel = new JPanel();
            chatMessagePanel.setLayout(new BoxLayout(chatMessagePanel, BoxLayout.Y_AXIS));
            chatMessagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // chatMessagePanel.setPreferredSize(new Dimension(500, 100));
            JScrollPane chatMessageScroll = new JScrollPane(chatMessagePanel);
            chatMessageScroll.getVerticalScrollBar().setUnitIncrement(16);
            chatMessageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // chatMessageScroll.setPreferredSize(new Dimension(600, 600));

            chatPanel.add(chatLabel);

            chatPanel.add(chatMessageScroll);

            JPanel chatAreaPanel = createChatArea();
            chatAreaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            // chatAreaPanel.setPreferredSize(new Dimension(0, 100));

            chatPanel.add(chatAreaPanel);

            return chatPanel;
        }

        void addMessages() {
            chatMessagePanel.removeAll();

            ArrayList<Message> messages = user.getMessages();

            messages.forEach((Message msg) -> {
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

        void addMessage(Message msg) {
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
            usernameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JLabel usnField = new JLabel(user.getUsername());
            usnField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            usnField.setMaximumSize(usnField.getPreferredSize());

            JLabel fullnameLabel = new JLabel("Full name");
            fullnameLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JTextField fnField = new JTextField(40);
            fnField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            fnField.setText(user.getFullname());

            fnField.setMaximumSize(fnField.getPreferredSize());

            JLabel addressLabel = new JLabel("Address");
            addressLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JTextField addrField = new JTextField(40);
            addrField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            addrField.setText(user.getAddress());

            addrField.setMaximumSize(addrField.getPreferredSize());

            JLabel dobLabel = new JLabel("Date of Birth");
            dobLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JTextField dField = new JTextField(40);
            dField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            dField.setText(user.getDob());

            dField.setMaximumSize(dField.getPreferredSize());

            JLabel genderLabel = new JLabel("Gender");
            genderLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JTextField gField = new JTextField(40);
            gField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            gField.setText(user.getGender());

            gField.setMaximumSize(gField.getPreferredSize());

            JLabel emailLabel = new JLabel("Email");
            emailLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            JTextField emField = new JTextField(40);
            emField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            emField.setText(user.getEmail());

            emField.setMaximumSize(emField.getPreferredSize());

            dialog.setSize(500, 600);

            dPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            dPanel.add(usernameLabel);
            dPanel.add(usnField);
            dPanel.add(fullnameLabel);
            dPanel.add(fnField);
            dPanel.add(addressLabel);
            dPanel.add(addrField);
            dPanel.add(dobLabel);
            dPanel.add(dField);
            dPanel.add(genderLabel);
            dPanel.add(gField);
            dPanel.add(emailLabel);
            dPanel.add(emField);

            dialog.add(dPanel);

            dialog.setLocationRelativeTo(null); // center the frame

            dialog.setResizable(false);

            // System.out.println(user.getFriends().get(0));

            return dialog;

        }

        JPanel createChatArea() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            JTextArea textArea = new JTextArea("Chat Here", 5, 20);
            textArea.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            textArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            // textArea.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            // scrollPane.setMaximumSize(new Dimension(800, 100));

            JButton btn = new JButton("Send");
            btn.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            // btn.setPreferredSize(new Dimension(100, 100));
            btn.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {

                    HashMap<String, String> packet = new HashMap<>();
                    packet.put("header", "chat");
                    packet.put("from", user.getUsername());
                    packet.put("to", curPeer);
                    packet.put("content", textArea.getText());

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    packet.put("time_stamp", dtf.format(now));
                    Message msg = new Message(user.getUsername(), curPeer, textArea.getText(), dtf.format(now));
                    Gson gson = new Gson();
                    String json = gson.toJson(packet);
                    System.out.println(json);
                    // SocketController sc = new SocketController();
                    // chatPW = sc.getChatWriter();
                    chatPW.println(json);

                    addMessage(msg);
                    // sc.close();
                }

            });

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(btn, BorderLayout.WEST);

            // panel.setVisible(false);

            return panel;
        }
    }
}
