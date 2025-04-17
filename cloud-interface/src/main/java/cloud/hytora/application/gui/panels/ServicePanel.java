
package cloud.hytora.application.gui.panels;

import javax.swing.*;

import cloud.hytora.application.gui.Application;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.*;

import java.awt.*;

public class ServicePanel extends JPanel {



    private final JLabel comingSoon;

    public ServicePanel() {
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
