package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.AuthService.AuthException;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.Role;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 230);
        setLocationRelativeTo(null);

        initUi();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

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

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> doLogin());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        setContentPane(panel);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            SessionContext session = authService.login(username, password);
            openDashboard(session);
            dispose();
        } catch (AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(SessionContext session) {
        Role role = session.getRole();
        SwingUtilities.invokeLater(() -> {
            if (role == Role.ADMIN) {
                new AdminDashboardFrame(session).setVisible(true);
            } else if (role == Role.INSTRUCTOR) {
                new InstructorDashboardFrame(session).setVisible(true);
            } else {
                new StudentDashboardFrame(session).setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
