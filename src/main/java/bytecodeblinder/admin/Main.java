package bytecodeblinder.admin;

import java.io.IOException;
import javax.swing.SwingUtilities;

public class Main {
    // Admin server = new Server();
    public static void main(String[] agrs) {
        SwingUtilities.invokeLater(() -> {
            try {
                AdminView serverView = new AdminView();
            } catch (IOException e) {

                e.printStackTrace();
            }
        });
    }
}
