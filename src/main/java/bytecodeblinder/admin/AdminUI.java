package bytecodeblinder.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.gson.*;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JYearChooser;

import bytecodeblinder.models.SocketController;
import bytecodeblinder.user.User;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.image.BufferedImage;

import java.util.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import io.github.cdimascio.dotenv.Dotenv;

class AdminView {
    private JFrame frame;
    private JPanel panel;
    private Admin admin;
    // private CardLayout cardLO;
    // private SocketController socketController;
    // Mode mode;
    private Dotenv dotenv = Dotenv.load();

    // User user;

    AdminView() throws IOException {

        Integer width = Integer.valueOf(dotenv.get("app.width"));
        Integer height = Integer.valueOf(dotenv.get("app.height"));

        frame = new JFrame(dotenv.get("app.title"));
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout());

        frame.setLocationRelativeTo(null); // center the frame

        frame.setResizable(false);

        // cardLO = new CardLayout();
        panel = new HomePanel();

        // panel.add("LOGIN", new MainPanel());
        // panel.add("SIGNUP", new SignupPanel());
        // panel.add("HOME", new HomePanel());

        // cardLO.show(panel, "LOGIN");
        // new SignupPanel(panel);
        frame.add(panel);
        frame.setVisible(true);
        admin = new Admin();
    }

    class HomePanel extends JPanel {
        // private JTextField usernameField;
        // private JTextField passwordField;
        // private JLabel messageHolder;
        JPanel mainPanel;

        HomePanel() throws IOException {
            super();
            GridBagConstraints gbc;

            setLayout(new GridBagLayout());
            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            // JPanel utilPanel = createSettingPanel();
            JPanel utilPanel = createUtilPanel();
            utilPanel.setLayout(new BoxLayout(utilPanel, BoxLayout.Y_AXIS));
            utilPanel.setBackground(Color.WHITE);
            gbc.gridx = 0;
            gbc.weightx = 1;
            // gbc.gridwidth = 1;
            gbc.weighty = 1;
            add(utilPanel, gbc);
            // add(Box.createRigidArea(new Dimension(10, 30)));

            mainPanel = new JPanel();
            // mainPanel.setBackground(Color.RED);

            gbc.gridx = 1;
            // gbc.weightx = 6;
            gbc.weightx = 8;
            // gbc.gridwidth = 6;
            gbc.weighty = 1;
            add(mainPanel, gbc);

        }

        JPanel createUtilPanel() {
            JPanel pan = new JPanel();
            JLabel chartLabel = new JLabel("Chart");
            chartLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            chartLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton subBtn = new JButton("Subcribers");
            subBtn.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            subBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            subBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "getallusers");

                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        System.out.println(res);
                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();
                        if (resHeader.equals("getallusersed")) {
                            JsonArray usersArray = resObject.getAsJsonArray("users");
                            ArrayList<User> users = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < usersArray.size(); i++) {

                                User u = gson.fromJson(usersArray.get(i), User.class);
                                // System.out.println(u.getUsername());
                                users.add(u);
                                System.out.println(u.getCreateDate());

                            }

                            admin.setUsers(users);

                        }

                        mainPanel.removeAll();
                        mainPanel.add(subcriberChart());
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    }).start();
                    // mainPanel.removeAll();
                    // mainPanel.revalidate();
                    // mainPanel.repaint();
                    // mainPanel.add(subcriberChart());
                }
            });

            JButton actBtn = new JButton("Active");
            actBtn.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            actBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            pan.add(chartLabel);
            pan.add(subBtn);
            pan.add(Box.createRigidArea(new Dimension(0, 10)));
            pan.add(actBtn);
            return pan;
        }

        JPanel subcriberChart() {
            ArrayList<User> users = admin.getUsers();

            JLabel yearLabel = new JLabel("Select year");
            yearLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            // yearField.setDate(new Date());

            TreeMap<Integer, int[]> data = new TreeMap<>();

            for (User user : users) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDate date = LocalDate.parse(user.getCreateDate(), formatter);

                // Lấy tháng và năm
                int month = date.getMonthValue();
                int year = date.getYear();

                data.putIfAbsent(year, new int[12]);

                data.get(year)[month - 1]++;

            }

            Set<Integer> years = data.keySet();

            // Create the combo box, select item at index 4.
            // Indices start at 0, so 4 specifies the pig.

            JPanel pan = new JPanel();
            JComboBox yearList = new JComboBox(years.toArray());
            yearList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    int year = (int) cb.getSelectedItem();

                    int[] month = data.get(year);
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int i = 0; i < 12; i++) {
                        dataset.addValue(month[i], String.valueOf(year), String.valueOf(i + 1));

                    }
                    JFreeChart chart = ChartFactory.createBarChart(
                            "Subcribers", // Chart title
                            "Month", // X-Axis label
                            "Quantity", // Y-Axis label
                            dataset // Data
                    );

                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartPanel.setPreferredSize(new Dimension(800, 600));
                    pan.setPreferredSize(new Dimension(800, 600));

                    pan.add(chartPanel);
                }
            });
            // petList.setSelectedIndex(4);
            // petList.addActionListener(this);

            // System.out.println(users.get(0).getCreateDate());
            // dataset.addValue(1, "Series1", "Category1");
            // dataset.addValue(4, "Series1", "Category2");
            // dataset.addValue(3, "Series1", "Category3");
            // dataset.addValue(5, "Series1", "Category4");
            pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
            pan.add(yearLabel);
            pan.add(yearList);
            return pan;
        }

    }

}
