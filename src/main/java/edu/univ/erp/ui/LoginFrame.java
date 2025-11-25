package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.domain.Role; // Assuming Role enum is here

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles the user login process for the ERP system.
 */
public class LoginFrame extends JFrame implements ActionListener {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    // Use the correct service class for authentication
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnLogin = new JButton("Login");

        panel.add(new JLabel("Username:"));
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);

        panel.add(new JLabel()); // Empty cell for alignment
        panel.add(btnLogin);

        btnLogin.addActionListener(this);
        txtPassword.addActionListener(this); // Allow login by pressing Enter in password field

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin || e.getSource() == txtPassword) {
            onLogin();
        }
    }

    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLogin.setEnabled(false);

        try {
            // This is the call that throws the Exception for invalid credentials OR concurrent login
            SessionContext session = authService.login(username, password);

            // Open the appropriate dashboard based on role
            openDashboard(session);

            dispose(); // Close the login frame upon successful login

        } catch (Exception ex) {
            // FIX: This now handles all authentication errors, including the
            // "User is already logged in on another terminal." message.
            JOptionPane.showMessageDialog(this,
                    "Login failed: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            btnLogin.setEnabled(true);
        }
    }

    private void openDashboard(SessionContext session) {
        // Use the role name from the session to determine which frame to open
        Role role = session.getRole();

        if (role == Role.ADMIN) {
            new AdminDashboardFrame(session).setVisible(true);
        } else if (role == Role.INSTRUCTOR) {
            new InstructorDashboardFrame(session).setVisible(true);
        } else if (role == Role.STUDENT) {
            new StudentDashboardFrame(session).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Unknown user role: " + role, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Basic main method to start the application
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}