
package cloud.hytora.application.bootstrap;

import java.util.prefs.Preferences;
import javax.swing.*;

import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.remote.Remote;
import com.formdev.flatlaf.*;
import cloud.hytora.application.gui.MainFrame;
import cloud.hytora.application.data.CloudTheme;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;


public class Bootstrap {

    public static void main(String[] args) {


        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "FlatLaf Demo");
            System.setProperty("apple.awt.application.appearance", "system");
        } else if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        //connecting to cloud node
        Remote.initFromOtherInstance(new RemoteIdentity("", "127.0.0.1", "Interface", 8876));

        SwingUtilities.invokeLater(() -> {

            Bootstrap.setup("/flatlaf-demo", CloudTheme.DARK); // TODO: 31.05.2022 store theme
            FlatLaf.registerCustomDefaultsSource("com.formdev.flatlaf.demo");
            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");

            // create frame
            MainFrame frame = new MainFrame();
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


    private static Preferences state;

    public static Preferences getState() {
        return state;
    }

    public static void setup(String rootPath, CloudTheme theme) {
        state = Preferences.userRoot().node(rootPath);
        switch (theme) {
            case DARK:
                FlatDarkLaf.setup();
                break;
            case LIGHT:
                FlatLightLaf.setup();
                break;
            case DARCULA:
                FlatDarculaLaf.setup();
                break;
        }

    }
}
