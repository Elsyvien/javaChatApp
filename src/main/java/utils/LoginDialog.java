package utils;

import java.nio.file.*;

import javax.swing.*;

public class LoginDialog {

    public static String[] showLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        String[] options = {"Login", "New User", "Cancel"};

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showOptionDialog(
            null,
            message,
            "Login",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (option == 0) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                System.err.println("[CLIENT] Username or password is empty.");
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return showLoginDialog(); // Retry login dialog
            }
            return new String[] {username, password};
        } else if (option == 1) {
            // Prompt for both new username and new password
            JTextField newUsernameField = new JTextField();
            JPasswordField newPasswordField = new JPasswordField();
            Object[] newUserMessage = {
                "Neuer Benutzername:", newUsernameField,
                "Neues Passwort:", newPasswordField
            };
            int newUserOption = JOptionPane.showConfirmDialog(
                null,
                newUserMessage,
                "Neuen Benutzer anlegen",
                JOptionPane.OK_CANCEL_OPTION
            );
            if (newUserOption == JOptionPane.OK_OPTION) {
                String newUsername = newUsernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                    usernameField.setText(newUsername);
                    passwordField.setText(newPassword);
                    return showLoginDialog();
                } else {
                    JOptionPane.showMessageDialog(null, "Benutzername und Passwort dürfen nicht leer sein.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return showLoginDialog();
                }
            } else {
                return showLoginDialog();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Login cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            System.err.println("[CLIENT] Login cancelled by user." + "\n" + "[CLIENT] Exiting application...");
            System.exit(0); // User cancelled → exit app
            return null; // will never reach here
        }
    }
}