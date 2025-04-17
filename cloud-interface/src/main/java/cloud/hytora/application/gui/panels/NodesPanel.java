
package cloud.hytora.application.gui.panels;

import cloud.hytora.application.gui.Application;

import javax.swing.*;
import java.awt.*;

public class NodesPanel extends JPanel {


    private final JLabel comingSoon;

    public NodesPanel() {
        comingSoon = new JLabel();
        comingSoon.setText("Coming soon!");
        comingSoon.setForeground(Color.RED);
        comingSoon.setFont(new Font("Arial", Font.BOLD, 30));

        int x = (int) (Application.getInstance().getBounds().getWidth() / 2);
        int y = (int) (Application.getInstance().getBounds().getHeight() / 2);

        comingSoon.setBounds(x, y, 300, 100);

        add(comingSoon);
    }
}
