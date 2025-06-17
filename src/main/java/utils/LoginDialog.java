package utils;

import utils.FileOperations;

import java.io.File;
import java.io.IOException;

import javax.swing.*;

public class LoginDialog {

    private static final String CREDENTIALS_PATH = "~/javaChatApp/UserData/credentials.txt";
    private static final FileOperations fileOps = new FileOperations(CREDENTIALS_PATH);

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
            return handleLogin(usernameField, passwordField);
        } else if (option == 1) {
            return handleNewUser();
        } else {
            JOptionPane.showMessageDialog(null, "Login cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            System.err.println("[CLIENT] Login cancelled by user.\n[CLIENT] Exiting application...");
            System.exit(0); // User cancelled → exit app
            return null; // will never reach here
        }
    }

    private static String[] handleLogin(JTextField usernameField, JPasswordField passwordField) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            System.err.println("[CLIENT] Username or password is empty.");
            JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return showLoginDialog(); // Retry login dialog
        }

        try {
            File file = new File(fileOps.getFilePath());
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            fileOps.writeToFile(username + ":" + password + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("[CLIENT] Error writing credentials to file: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error saving login data! Check log for details.", "Error", JOptionPane.ERROR_MESSAGE);
            return showLoginDialog(); // Retry login dialog
        }

        return new String[] {username, password};
    }

    private static String[] handleNewUser() {
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
                try {
                    File file = new File(fileOps.getFilePath());
                    File parentDir = file.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    fileOps.writeToFile(newUsername + ":" + newPassword + System.lineSeparator());
                    JOptionPane.showMessageDialog(null, "Benutzer erfolgreich angelegt.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    System.err.println("[CLIENT] Error writing new user to file: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Fehler beim Speichern des Benutzers!", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
                return showLoginDialog();
            } else {
                JOptionPane.showMessageDialog(null, "Benutzername und Passwort dürfen nicht leer sein.", "Fehler", JOptionPane.ERROR_MESSAGE);
                return showLoginDialog();
            }
        } else {
            return showLoginDialog();
        }
    }
}