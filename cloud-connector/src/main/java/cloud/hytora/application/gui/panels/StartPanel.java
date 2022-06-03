
package cloud.hytora.application.gui.panels;

import javax.swing.*;

import net.miginfocom.swing.*;

public class StartPanel extends JPanel {

    private final JProgressBar progressBar3;
    private final JProgressBar progressBar4;
    private final JSlider slider3;
    private final JProgressBar progressBar1;
    private final JProgressBar progressBar2;

    public StartPanel() {

        progressBar1 = new JProgressBar();
        progressBar2 = new JProgressBar();
        progressBar3 = new JProgressBar();
        progressBar4 = new JProgressBar();

        slider3 = new JSlider();
        JSlider slider5 = new JSlider();
        JLabel progressBarLabel = new JLabel();

        setLayout(new MigLayout(
                "insets dialog,hidemode 3",
                // columns
                "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]",
                // rows
                "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[100,top]"));

        //---- slider3 ----
        slider3.setMinorTickSpacing(10);
        slider3.setPaintTicks(true);
        slider3.setMajorTickSpacing(50);
        slider3.setPaintLabels(true);
        slider3.setValue(30);
        slider3.addChangeListener(e -> changeProgress());
        add(slider3, "cell 1 7 3 1,aligny top,grow 100 0");

        //---- progressBarLabel ----
        progressBarLabel.setText("JProgressBar:");
        add(progressBarLabel, "cell 0 8");

        //---- progressBar2 ----
        progressBar2.setStringPainted(true);
        progressBar2.setValue(60);
        add(progressBar2, "cell 1 8 3 1,growx");


    }

    private void changeProgress() {
        int value = slider3.getValue();
        progressBar1.setValue(value);
        progressBar2.setValue(value);
        progressBar3.setValue(value);
        progressBar4.setValue(value);
    }

}
