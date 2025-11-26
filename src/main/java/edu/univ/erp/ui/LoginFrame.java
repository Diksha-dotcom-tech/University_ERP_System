package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.Role;
import edu.univ.erp.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles the user login process for the ERP system.
 */
public class LoginFrame extends JFrame implements ActionListener {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnLogin = new JButton("Login");

        panel.add(new JLabel("Username:"));
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);

        panel.add(new JLabel());
        panel.add(btnLogin);

        btnLogin.addActionListener(this);
        txtPassword.addActionListener(this); // Enter in password field

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
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLogin.setEnabled(false);

        try {
            SessionContext session = authService.login(username, password);
            openDashboard(session);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login failed: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            btnLogin.setEnabled(true);
        }
    }

    private void openDashboard(SessionContext session) {
        Role role = session.getRole();

        if (role == Role.ADMIN) {
            new AdminDashboardFrame(session).setVisible(true);
        } else if (role == Role.INSTRUCTOR) {
            new InstructorDashboardFrame(session).setVisible(true);
        } else if (role == Role.STUDENT) {
            new StudentDashboardFrame(session).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unknown user role: " + role,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
