import javax.swing.*;
import java.awt.*;

public class MChat {
    private JTextField textField1;
    private JButton button1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MChat");
            MChat mChat = new MChat();
            frame.setContentPane(mChat.createUI());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    private JPanel createUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        textField1 = new JTextField(20);
        button1 = new JButton("Button");

        button1.addActionListener(e -> {
            String text = textField1.getText();
            JOptionPane.showMessageDialog(panel, "You entered: " + text);
            System.out.println("You entered: " + text);
        });

        textField1.addActionListener(e -> button1.doClick());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textField1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(button1, gbc);

        return panel;
    }
}
