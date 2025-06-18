package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;

import utils.FileOperations;
/**
 * A dialog for user login and registration.
 * It allows users to log in with existing credentials or register a new account.
 * The user data is stored in a simple text file.
 * @author Max Staneker, Mia Schienagel
 */

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private String loggedInUser;

    private final FileOperations fileOps = new FileOperations();
    private static final String USER_FILE = "UserData/users.txt";

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> onLogin());
        registerButton.addActionListener(e -> onRegister());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void onLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            System.err.println("[CLIENT] Please enter username and password");
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (checkCredentials(username, password)) {
                loggedInUser = username;
                System.out.println("[CLIENT] User crendentials have been found...proceeding");
                JOptionPane.showMessageDialog(this, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                System.err.println("[CLIENT] Invalid credentials for user: " + username);
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            System.err.println("[CLIENT] Error reading user file: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error reading user file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            System.out.println("[CLIENT] Please enter username and password");
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            fileOps.appendLine(USER_FILE, username + ":" + password);
            System.out.println("[CLIENT] User registered: " + username);
            JOptionPane.showMessageDialog(this, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            System.err.println("[CLIENT] Error writing user file: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error writing user file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkCredentials(String username, String password) throws IOException {
        List<String> lines = fileOps.readAllLines(USER_FILE);
        for (String line : lines) { // Binary Seach 
            String[] parts = line.split(":", 2);
            if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shows the dialog and returns the username of the logged in user.
     * Returns {@code null} if the dialog was closed without a successful login.
     */
    public String showDialog() {
        setVisible(true);
        return loggedInUser;
    }
}