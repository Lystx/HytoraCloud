
package cloud.hytora.application.bootstrap;

import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import cloud.hytora.application.elements.data.ApplicationData;
import com.formdev.flatlaf.*;
import cloud.hytora.application.elements.data.CloudTheme;
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

        /*
        //connecting to cloud node
        Remote.initFromOtherInstance(RemoteIdentity.forApplication(new ProtocolAddress(host, port, key)), packet -> {

            Task.runAsync(() -> {
                try {
                    GitHub github = GitHubBuilder.fromEnvironment().newInstance();
                    GHRepository repository = github.getRepository("Lystx/HytoraCloud");
                    Collection<GHCommit> cachedCommits = repository.listCommits().toList();
                    CloudDriver.retrieveFromStorage().getEventManager().callEventGlobally(new CommitHistoryLoadedEvent(cachedCommits));
                    CloudDriver.retrieveFromStorage().getLogger().info("Loaded GitHub data");
                } catch (Exception e) {
                    CloudDriver.retrieveFromStorage().getLogger().info("Couldn't load GitHub data");
                    e.printStackTrace();
                }
            });

            Bootstrap.setup(new ApplicationData(1, CloudTheme.DARK)); // TODO: 31.05.2022 store theme


            SwingUtilities.invokeLater(() -> {

                try {

                    // create frame
                    Application instance = new Application();

                    //first boxes => then init
                    instance.registerInfoBox(new StartPanelInfoBox(0x00, "Players", "-1/0 Online", "users", () -> Remote.retrieveFromStorage().getPlayerManager().getCloudPlayerOnlineAmount() + "/" + Remote.retrieveFromStorage().getPlayerManager().countPlayerCapacity() + " Online"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x01, "Nodes", "-1/0 Connected", "show", () -> Remote.retrieveFromStorage().getNodeManager().getAllConnectedNodes().size() + " Connected"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x02, "Services", "-1 Online", "services", () -> Remote.retrieveFromStorage().getServiceManager().getAllCachedServices().size() + " Online"));
                    instance.registerInfoBox(new StartPanelInfoBox(0x03, "Configurations", "-1 Loaded", "services", () -> Remote.retrieveFromStorage().getServiceTaskManager().getAllCachedTasks().size() + " Loaded"));

                    instance.init();

                    instance.pack();
                    instance.setSize(1000, 850);

                    instance.setResizable(false);
                    instance.setLocationRelativeTo(null);
                    instance.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        },  () -> {
            System.exit(0);
        });*/

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
