package user;

import javax.imageio.ImageIO;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;

import com.toedter.calendar.JDateChooser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import config.AppConfig;

class UserView {
    private JFrame frame;
    private JPanel panel;
    private CardLayout cardLO;
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

        frame.setResizable(false);
        mode = Mode.LOGIN;

        cardLO = new CardLayout();
        panel = new JPanel(cardLO);

        panel.add("LOGIN", new LoginPanel(cardLO, panel));
        panel.add("SIGNUP", new SignupPanel(cardLO, panel));

        cardLO.show(panel, "LOGIN");
        // new SignupPanel(panel);
        frame.add(panel);
        frame.setVisible(true);
    }

}

class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JTextField passwordField;
    private CardLayout cardLO;
    private JPanel container;

    LoginPanel(CardLayout cardLO, JPanel container) throws IOException {

        super(new BorderLayout());
        this.cardLO = cardLO;
        this.container = container;

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
        questionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        questionText.setForeground(new Color(82, 82, 82));
        instructionText.setFont(new Font("Nunito Sans", Font.BOLD, 18));
        instructionText.setForeground(new Color(127, 38, 91));
        instructionText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        instructionText.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                instructionText.setText("<html><u>Create an account</u></html>");
            }

            public void mouseExited(MouseEvent me) {
                instructionText.setText("<html>Create an account</html>");
            }

            public void mouseClicked(MouseEvent me) {
                cardLO.show(container, "SIGNUP");
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
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField fullnameField;
    private JTextField emailField;
    private JTextField addressField;
    private JDateChooser dobField;

    private CardLayout cardLO;
    private JPanel container;

    // private JTextField addressField;
    // tên đăng nhập, họ tên, địa chỉ, ngàysinh, giới tính, emai
    SignupPanel(CardLayout cardLO, JPanel container) throws IOException {
        super(new BorderLayout());
        this.cardLO = cardLO;
        this.container = container;
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
        // dobField.setPreferredSize(new Dimension(330, 80));
        dobField.setMaximumSize(new Dimension(770, 80));
        dobField.setFont(new Font("Nunito Sans", Font.PLAIN, 20));
        dobField.setDate(new Date());
        // dobField.setMaximumSize(new Dimension(380, 80));

        JLabel genderLabel = new JLabel("Gender");
        genderLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

        JRadioButton maleRadio = new JRadioButton("Male");
        JRadioButton femaleRadio = new JRadioButton("Female");
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
                cardLO.show(container, "LOGIN");
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

}