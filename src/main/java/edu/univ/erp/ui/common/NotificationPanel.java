package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

public class NotificationPanel extends JPanel {

    private final JTextArea area;

    public NotificationPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Notifications"));

        area = new JTextArea(4, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        add(new JScrollPane(area), BorderLayout.CENTER);
    }

    public void notifyMessage(String msg) {
        if (!msg.endsWith("\n")) msg = msg + "\n";
        area.append(msg);
        area.setCaretPosition(area.getDocument().getLength());
    }
}
