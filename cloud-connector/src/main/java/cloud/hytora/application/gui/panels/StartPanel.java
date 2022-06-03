
package cloud.hytora.application.gui.panels;

import javax.swing.*;

import cloud.hytora.application.elements.StartPanelInfoBox;
import cloud.hytora.application.gui.MainFrame;
import cloud.hytora.driver.CloudDriver;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StartPanel extends JPanel {

    private final JProgressBar progressBar3;
    private final JProgressBar progressBar4;
    private final JSlider slider3;
    private final JProgressBar progressBar1;
    private final JProgressBar progressBar2;

    private final MainFrame instance;

    private int boxStartX = 10;
    private int boxStartY = 5;

    private Map<Integer, JOptionPane> boxPanes = new HashMap<>();

    public StartPanel(MainFrame instance) {
        this.instance = instance;

        progressBar1 = new JProgressBar();
        progressBar2 = new JProgressBar();
        progressBar3 = new JProgressBar();
        progressBar4 = new JProgressBar();

        slider3 = new JSlider();
        JLabel progressBarLabel = new JLabel();

        setLayout(null);

        slider3.setMinorTickSpacing(10);
        slider3.setPaintTicks(true);
        slider3.setMajorTickSpacing(50);
        slider3.setPaintLabels(true);
        slider3.setValue(30);
        slider3.addChangeListener(e -> changeProgress());
        //add(slider3, "cell 1 7 3 1,aligny top,grow 100 0");

        for (StartPanelInfoBox box : instance.getBoxes().values()) {
            createBox(box);
        }

        //---- progressBarLabel ----
        progressBarLabel.setText("JProgressBar:");
        //add(progressBarLabel, "cell 0 8");

        //---- progressBar2 ----
        progressBar2.setStringPainted(true);
        progressBar2.setValue(60);
        //add(progressBar2, "cell 1 8 3 1,growx");

        CloudDriver.getInstance().getScheduler().scheduleRepeatingTask(() -> {
            for (Integer boxId : boxPanes.keySet()) {
                JOptionPane pane = boxPanes.get(boxId);
                StartPanelInfoBox infoBox = instance.getInfoBox(boxId);
                infoBox.setContent(infoBox.getTextUpdater().get());

                pane.setMessage(new Object[]{
                        infoBox.getContent(),
                        "==> " + infoBox.getTitle()
                });
            }
        }, 0L, TimeUnit.SECONDS.toMillis(1));
    }

    public void createBox(StartPanelInfoBox box) {

        int width = 150;
        int height = 50;

        JOptionPane pane = new JOptionPane();

        pane.setMessage(new Object[]{
                box.getContent(),
                "==> " + box.getTitle()
        });
        pane.setOptions(new Object[0]);


        if (box.getResourceIcon() != null && !box.getResourceIcon().trim().isEmpty()) {
            pane.setIcon(new FlatSVGIcon("cloud/hytora/img/" + box.getResourceIcon() + ".svg", 20, 20));
        }
        pane.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        pane.setEnabled(true);
        pane.setVisible(true);
        pane.setBounds(boxStartX, boxStartY, width, height);
        pane.setName("CustomBox#" + box.getId());

        add(pane);
        boxPanes.put(box.getId(), pane);

        boxStartX = width + 20 + boxStartX;
    }

    private void changeProgress() {
        int value = slider3.getValue();
        progressBar1.setValue(value);
        progressBar2.setValue(value);
        progressBar3.setValue(value);
        progressBar4.setValue(value);
    }

}
