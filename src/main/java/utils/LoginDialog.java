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
            if (username.isEmpty() || password.isEmpty()) {
                System.err.println("[CLIENT] Username or password is empty.");
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return showLoginDialog(); // Retry login dialog
            }
            return new String[] {username, password};
        } else {
            JOptionPane.showMessageDialog(null, "Login cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            System.err.println("[CLIENT] Login cancelled by user." + "\n" + "[CLIENT] Exiting application...");
            System.exit(0); // User cancelled → exit app
            return null; // will never reach here
        }
    }
}