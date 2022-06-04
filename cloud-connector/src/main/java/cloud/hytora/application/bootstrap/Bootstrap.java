
package cloud.hytora.application.bootstrap;

import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import cloud.hytora.application.data.ApplicationData;
import cloud.hytora.application.elements.StartPanelInfoBox;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.remote.Remote;
import com.formdev.flatlaf.*;
import cloud.hytora.application.gui.Application;
import cloud.hytora.application.data.CloudTheme;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;


public class Bootstrap {

    public static void main(String[] args) {

        List<String> arguments = Arrays.asList(args);

        if (arguments.stream().noneMatch(s -> s.startsWith("host=")) || arguments.stream().noneMatch(s -> s.startsWith("port=")) || arguments.stream().noneMatch(s -> s.startsWith("key="))) {
            throw new IllegalStateException("Can't start Application without following arguments: host=??? port=??? key=???");
        }


        String host = "";
        String key = "";
        int port = -1;

        for (String argument : arguments) {
            if (argument.startsWith("host=")) {
                host = argument.split("host=")[1];
            }
            if (argument.startsWith("port=")) {
                port = Integer.parseInt(argument.split("port=")[1]);
            }
            if (argument.startsWith("key=")) {
                key = argument.split("key=")[1];
            }
        }

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "FlatLaf Demo");
            System.setProperty("apple.awt.application.appearance", "system");
        } else if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        //connecting to cloud node
        Remote.initFromOtherInstance(new RemoteIdentity(key, "", host, "Application", port), packet -> {
            SwingUtilities.invokeLater(() -> {

                Bootstrap.setup(new ApplicationData(1, CloudTheme.DARK)); // TODO: 31.05.2022 store theme
                FlatLaf.registerCustomDefaultsSource("com.formdev.flatlaf.demo");
                FlatInspector.install("ctrl shift alt X");
                FlatUIDefaultsInspector.install("ctrl shift alt Y");

                try {

                    // create frame
                    Application instance = new Application();

                    //first boxes => then init
                    instance.registerInfoBox(new StartPanelInfoBox(0x00, "Players", "-1/0 Online", "users", () -> Remote.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() + "/" + Remote.getInstance().getPlayerManager().countPlayerCapacity() + " Online"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x01, "Nodes", "-1/0 Connected", "show", () -> Remote.getInstance().getNodeManager().getAllConnectedNodes().size() + " Connected"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x02, "Services", "-1 Online", "services", () -> Remote.getInstance().getServiceManager().getAllCachedServices().size() + " Online"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x03, "Configurations", "-1 Loaded", "services", () -> Remote.getInstance().getConfigurationManager().getAllCachedConfigurations().size() + " Loaded"));

                    instance.init();

                    instance.pack();
                    instance.setResizable(false);
                    instance.setLocationRelativeTo(null);
                    instance.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }, () -> {
            System.exit(0);
        });

    }

    public static void setup(ApplicationData data) {
        CloudTheme theme = data.getTheme();
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
