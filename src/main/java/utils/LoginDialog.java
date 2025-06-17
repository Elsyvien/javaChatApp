package utils;

import javax.swing.*;

public class LoginDialog {

    public static String[] showLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
                "Username:", usernameField,
                "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Login",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            return new String[] {username, password};
        } else {
            System.exit(0); // User cancelled → exit app
            return null; // will never reach here
        }
    }

    public static void main(String[] args) {
        // 1️⃣ Login Dialog vor UI:
        String[] credentials = showLoginDialog();
        String username = credentials[0];
        String password = credentials[1];

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // 2️⃣ Danach deine Chat-UI starten:
        SwingUtilities.invokeLater(() -> {
            // z.B. new ChatClientGUI(username).setVisible(true);
        });
    }
}