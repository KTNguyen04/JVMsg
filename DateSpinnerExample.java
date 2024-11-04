import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

public class DateSpinnerExample {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Date Spinner Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);

        // Create a Calendar instance for the current date
        Calendar calendar = Calendar.getInstance();

        // Create a SpinnerDateModel
        SpinnerDateModel model = new SpinnerDateModel();
        model.setValue(calendar.getTime()); // Set the current date

        // Create a JSpinner with the SpinnerDateModel
        JSpinner dateSpinner = new JSpinner(model);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // Add the spinner to the frame
        frame.setLayout(new FlowLayout());
        frame.add(dateSpinner);

        // Show the frame
        frame.setVisible(true);
    }
}
