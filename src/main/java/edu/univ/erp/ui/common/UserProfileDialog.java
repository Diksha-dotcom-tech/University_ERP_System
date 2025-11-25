package edu.univ.erp.ui.common;

import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.service.ProfileService;
import edu.univ.erp.service.ProfileService.ProfileDto;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * Modal dialog to display role-specific profile information.
 */
public class UserProfileDialog extends JDialog {

    private final SessionContext session;
    private final ProfileService profileService = new ProfileService();

    private JLabel lblUsername;
    private JLabel lblRole;
    private JTextArea txtPrimaryInfo;
    private JLabel lblLastLogin;
    private JLabel lblLoading;

    public UserProfileDialog(Frame owner, SessionContext session) {
        super(owner, "My Profile", true);
        this.session = session;

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUi();
        loadProfileData();
    }

    private void initUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Title and User
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel title = new JLabel("Your Account Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Username:"), gbc);
        lblUsername = new JLabel("...");
        lblUsername.setFont(lblUsername.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; row++;
        panel.add(lblUsername, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Role:"), gbc);
        lblRole = new JLabel("...");
        lblRole.setFont(lblRole.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; row++;
        panel.add(lblRole, gbc);

        // Primary Info (Dynamic Content Area)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(new JLabel("Role-Specific Info:"), gbc);

        txtPrimaryInfo = new JTextArea(4, 25);
        txtPrimaryInfo.setEditable(false);
        txtPrimaryInfo.setLineWrap(true);
        txtPrimaryInfo.setBackground(panel.getBackground());

        gbc.gridy = row + 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane scroll = new JScrollPane(txtPrimaryInfo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, gbc);

        row += 2;

        // Last Login
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Last Login:"), gbc);
        lblLastLogin = new JLabel("...");
        gbc.gridx = 1; row++;
        panel.add(lblLastLogin, gbc);

        // Loading Indicator
        lblLoading = new JLabel("Loading profile...", SwingConstants.CENTER);
        lblLoading.setVisible(true);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        add(lblLoading, BorderLayout.CENTER);
        add(closeBtn, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
    }

    /**
     * Loads profile data asynchronously since it hits the database.
     */
    private void loadProfileData() {
        lblLoading.setVisible(true);

        SwingWorker<ProfileDto, Void> worker = new SwingWorker<>() {
            @Override
            protected ProfileDto doInBackground() throws Exception {
                return profileService.getProfile(session);
            }

            @Override
            protected void done() {
                lblLoading.setVisible(false);
                try {
                    ProfileDto dto = get();
                    lblUsername.setText(dto.username);
                    lblRole.setText(dto.role);
                    txtPrimaryInfo.setText(dto.primaryInfo);
                    lblLastLogin.setText(dto.lastLogin != null ? dto.lastLogin : "N/A");

                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(UserProfileDialog.this,
                            "Failed to load profile: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}