
package cloud.hytora.application.gui.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.*;

import cloud.hytora.application.gui.Application;
import com.formdev.flatlaf.FlatLaf;
import net.miginfocom.swing.*;

public class ModulesPanel extends JPanel {


    private final JLabel comingSoon;

    public ModulesPanel() {
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
