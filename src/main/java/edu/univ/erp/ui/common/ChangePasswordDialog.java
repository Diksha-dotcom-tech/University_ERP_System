package edu.univ.erp.ui.common;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.auth.AuthException;
import edu.univ.erp.auth.SessionContext;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final SessionContext session;
    private final AuthApi authApi = new AuthApi();

    private JPasswordField currentField;
    private JPasswordField newField;
    private JPasswordField confirmField;

    public ChangePasswordDialog(Frame owner, SessionContext session) {
        super(owner, "Change Password", true);
        this.session = session;

        setSize(350, 220);
        setLocationRelativeTo(owner);

        initUi();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Current password:"), gbc);
        currentField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(currentField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("New password:"), gbc);
        newField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(newField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Confirm new:"), gbc);
        confirmField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(confirmField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton okBtn = new JButton("Change");
        okBtn.addActionListener(e -> doChange());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(okBtn);
        buttons.add(cancelBtn);

        panel.add(buttons, gbc);

        setContentPane(panel);
    }

    private void doChange() {
        String current = new String(currentField.getPassword());
        String np = new String(newField.getPassword());
        String cp = new String(confirmField.getPassword());

        if (current.isEmpty() || np.isEmpty() || cp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }
        if (!np.equals(cp)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.");
            return;
        }
        if (np.length() < 6) {
            JOptionPane.showMessageDialog(this, "Use at least 6 characters for the new password.");
            return;
        }

        try {
            authApi.changePassword(session, current, np);
            JOptionPane.showMessageDialog(this, "Password changed successfully.");
            dispose();
        } catch (AuthException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Change password failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
