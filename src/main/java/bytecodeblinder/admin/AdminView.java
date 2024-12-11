package bytecodeblinder.admin;

import javax.swing.*;
import com.google.gson.*;
import bytecodeblinder.controller.SocketController;
import bytecodeblinder.user.User;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private Dotenv dotenv = Dotenv.load();

    AdminView() throws IOException {

        Integer width = Integer.valueOf(dotenv.get("app.width"));
        Integer height = Integer.valueOf(dotenv.get("app.height"));

        frame = new JFrame(dotenv.get("app.title"));
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new CardLayout());

        frame.setLocationRelativeTo(null);

        frame.setResizable(false);

        panel = new HomePanel();

        frame.add(panel);
        frame.setVisible(true);
        admin = new Admin();
    }

    class HomePanel extends JPanel {

        JPanel mainPanel;

        HomePanel() throws IOException {
            super();
            GridBagConstraints gbc;

            setLayout(new GridBagLayout());
            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            JPanel utilPanel = createUtilPanel();
            utilPanel.setLayout(new BoxLayout(utilPanel, BoxLayout.Y_AXIS));
            utilPanel.setBackground(Color.WHITE);
            gbc.gridx = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            add(utilPanel, gbc);

            mainPanel = new JPanel();

            gbc.gridx = 1;
            gbc.weightx = 8;
            gbc.weighty = 1;
            add(mainPanel, gbc);

        }

        JPanel createUtilPanel() {
            JPanel pan = new JPanel();
            JLabel chartLabel = new JLabel("Chart");
            chartLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            chartLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton subBtn = new JButton("Subscribers");
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

                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();
                        if (resHeader.equals("getallusersed")) {
                            JsonArray usersArray = resObject.getAsJsonArray("users");
                            ArrayList<User> users = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < usersArray.size(); i++) {

                                User u = gson.fromJson(usersArray.get(i), User.class);

                                users.add(u);

                            }

                            admin.setUsers(users);

                        }

                        mainPanel.removeAll();
                        mainPanel.add(subscriberChart());
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    }).start();

                }
            });

            JButton actBtn = new JButton("Active");
            actBtn.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            actBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "getlogindata");

                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();
                        if (resHeader.equals("getlogindataed")) {
                            JsonArray logsArray = resObject.getAsJsonArray("logs");
                            ArrayList<LoginLog> logs = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < logsArray.size(); i++) {

                                LoginLog u = gson.fromJson(logsArray.get(i), LoginLog.class);

                                logs.add(u);

                            }

                            admin.setLoginLogs(logs);

                        }

                        mainPanel.removeAll();
                        mainPanel.add(activeChart());
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    }).start();

                }
            });
            JLabel logLabel = new JLabel("Log");
            logLabel.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            logLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JButton loginLogBtn = new JButton("Login Log");
            loginLogBtn.setFont(new Font("Nunito Sans", Font.BOLD, 22));
            loginLogBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            loginLogBtn.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new Thread(() -> {
                        SocketController sc = new SocketController();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("header", "getlogindata");

                        sc.sendRequest(jsonObject.toString());

                        String res = sc.getResponse();
                        sc.close();

                        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
                        String resHeader = resObject.get("header").getAsString();
                        if (resHeader.equals("getlogindataed")) {
                            JsonArray logsArray = resObject.getAsJsonArray("logs");
                            ArrayList<LoginLog> logs = new ArrayList<>();
                            Gson gson = new Gson();
                            for (int i = 0; i < logsArray.size(); i++) {

                                LoginLog u = gson.fromJson(logsArray.get(i), LoginLog.class);

                                logs.add(u);

                            }

                            admin.setLoginLogs(logs);

                        }

                        mainPanel.removeAll();
                        mainPanel.add(loginLogTable());
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    }).start();

                }
            });

            pan.add(chartLabel);
            pan.add(subBtn);
            pan.add(Box.createRigidArea(new Dimension(0, 10)));
            pan.add(actBtn);
            pan.add(Box.createRigidArea(new Dimension(0, 10)));
            pan.add(logLabel);
            pan.add(loginLogBtn);
            return pan;
        }

        JPanel subscriberChart() {
            ArrayList<User> users = admin.getUsers();

            JLabel yearLabel = new JLabel("Select year");
            yearLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            TreeMap<Integer, int[]> data = new TreeMap<>();

            for (User user : users) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDate date = LocalDate.parse(user.getCreateDate(), formatter);

                int month = date.getMonthValue();
                int year = date.getYear();

                data.putIfAbsent(year, new int[12]);

                data.get(year)[month - 1]++;

            }

            Set<Integer> years = data.keySet();

            JPanel pan = new JPanel();
            JComboBox yearList = new JComboBox(years.toArray());
            yearList.setFont(new Font("Nunito Sans", Font.PLAIN, 20));

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
                            "subscribers",
                            "Month",
                            "Quantity",
                            dataset);

                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartPanel.setPreferredSize(new Dimension(800, 600));
                    pan.setPreferredSize(new Dimension(800, 650));

                    pan.removeAll();
                    pan.add(yearLabel);
                    pan.add(yearList);
                    pan.add(chartPanel);
                    pan.revalidate();
                    pan.repaint();
                }
            });

            pan.add(yearLabel);
            pan.add(yearList);
            return pan;
        }

        JPanel activeChart() {
            ArrayList<LoginLog> logs = admin.getLoginLogs();

            JLabel yearLabel = new JLabel("Select year");
            yearLabel.setFont(new Font("Nunito Sans", Font.PLAIN, 22));

            TreeMap<Integer, int[]> data = new TreeMap<>();

            for (LoginLog log : logs) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDate date = LocalDate.parse(log.getLoginTime(), formatter);

                int month = date.getMonthValue();
                int year = date.getYear();

                data.putIfAbsent(year, new int[12]);

                data.get(year)[month - 1]++;

            }

            Set<Integer> years = data.keySet();

            JPanel pan = new JPanel();
            JComboBox yearList = new JComboBox(years.toArray());
            yearList.setFont(new Font("Nunito Sans", Font.PLAIN, 20));

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
                            "Active Count",
                            "Month",
                            "Quantity",
                            dataset);

                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartPanel.setPreferredSize(new Dimension(800, 600));
                    pan.setPreferredSize(new Dimension(800, 650));

                    pan.removeAll();
                    pan.add(yearLabel);
                    pan.add(yearList);
                    pan.add(chartPanel);
                    pan.revalidate();
                    pan.repaint();
                }
            });

            pan.add(yearLabel);
            pan.add(yearList);
            return pan;
        }

        JScrollPane loginLogTable() {
            ArrayList<LoginLog> logs = admin.getLoginLogs();

            ArrayList<ArrayList<String>> data = new ArrayList<>();

            JTable table;
            String[] columnNames = { "Time", "Username", "Fullname" };

            for (LoginLog log : logs) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(log.getLoginTime(), formatter);
                String formattedLoginTime = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                String username = log.getUsername();
                String fullname = log.getFullname();
                String loginTime = formattedLoginTime;
                ArrayList<String> row = new ArrayList<>();
                row.add(loginTime);
                row.add(username);
                row.add(fullname);
                data.add(row);
            }
            String[][] tableData = data.stream()
                    .map(row -> row.toArray(new String[0]))
                    .toArray(String[][]::new);
            table = new JTable(tableData, columnNames);
            table.setEnabled(false);

            table.setFont(new Font("Arial", Font.PLAIN, 18));

            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
            table.setRowHeight(30);

            JScrollPane scrollPane = new JScrollPane(table);

            scrollPane.setPreferredSize(new Dimension(700, 750));

            return scrollPane;
        }

    }

}
