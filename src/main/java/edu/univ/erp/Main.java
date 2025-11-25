package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
