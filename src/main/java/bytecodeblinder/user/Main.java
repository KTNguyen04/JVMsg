package bytecodeblinder.user;

import java.io.IOException;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] agrs) {
        SwingUtilities.invokeLater(() -> {
            try {
                UserView userView = new UserView();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
