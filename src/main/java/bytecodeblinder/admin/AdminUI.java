package bytecodeblinder.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.gson.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
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
                    mainPanel.removeAll();
                    mainPanel.revalidate();
                    mainPanel.repaint();
                    mainPanel.add(subcriberChart());
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

        ChartPanel subcriberChart() {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1, "Series1", "Category1");
            dataset.addValue(4, "Series1", "Category2");
            dataset.addValue(3, "Series1", "Category3");
            dataset.addValue(5, "Series1", "Category4");

            JFreeChart chart = ChartFactory.createBarChart(
                    "Subcribers", // Chart title
                    "Month", // X-Axis label
                    "Quantity", // Y-Axis label
                    dataset // Data
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 600));

            return chartPanel;
        }

    }

}
