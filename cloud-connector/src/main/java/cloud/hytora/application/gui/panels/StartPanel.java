
package cloud.hytora.application.gui.panels;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import cloud.hytora.application.bootstrap.Bootstrap;
import cloud.hytora.application.data.CloudUpdateInfo;
import cloud.hytora.application.elements.StartPanelInfoBox;
import cloud.hytora.application.elements.event.CommitHistoryLoadedEvent;
import cloud.hytora.application.gui.Application;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.CloudEventHandler;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.PagedIterator;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StartPanel extends JPanel {

    private final JProgressBar progressBar3;
    private final JProgressBar progressBar4;
    private final JProgressBar progressBar1;
    private final JProgressBar progressBar2;
    private JTable updateTable;

    private int boxStartX = 65;
    private int boxStartY = 20;

    private Map<Integer, JOptionPane> boxPanes = new HashMap<>();

    public StartPanel(Application instance) throws IOException {

        progressBar1 = new JProgressBar();
        progressBar2 = new JProgressBar();
        progressBar3 = new JProgressBar();
        progressBar4 = new JProgressBar();

        setLayout(null);




        for (StartPanelInfoBox box : instance.getBoxes().values()) {
            createBox(box);
        }

        try {
            JLabel banner = new JLabel(new ImageIcon(ImageIO.read(Bootstrap.class.getResourceAsStream("/cloud/hytora/img/banner.png"))));

            banner.setBounds(30, 115, banner.getWidth(), banner.getHeight());
            add(banner);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //---- progressBar2 ----
        progressBar2.setStringPainted(true);
        progressBar2.setValue(60);
        //add(progressBar2, "cell 1 8 3 1,growx");


        JScrollPane scrollPane = new JScrollPane();
        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(new JMenuItem("Some Action"));
        popupMenu.add(new JMenuItem("More Action"));
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem("More More Action"));

        updateTable = new JTable();
        updateTable.setShowHorizontalLines(true);
        updateTable.setShowVerticalLines(true);


        try {
            this.updateTable(updateTable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateTable.setAutoCreateRowSorter(true);
        updateTable.setComponentPopupMenu(popupMenu);

        scrollPane.setViewportView(updateTable);
        scrollPane.setBounds(65, 300, 800, 400);

        add(scrollPane);

        CloudDriver.getInstance().getEventManager().registerListener(this);

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


    private void updateTable(JTable updateTable) throws Exception {

        InputStream input = new URL("https://github.com/Lystx/HytoraCloud").openStream();

        Document document = DocumentFactory.newJsonDocument(input);
        Bundle updates = document.getBundle("updates");

        String[] rows = new String[]{"Date", "From", "Message", "Files affected"};
        Object[][] tableContent = new Object[updates.size()][rows.length];

        for (int i = 0; i < updates.size(); i++) {

            IEntry entry = updates.getEntry(i);
            Document doc = entry.toDocument();
            CloudUpdateInfo updateInfo = doc.toInstance(CloudUpdateInfo.class);

            tableContent[i] = updateInfo.toArray();
        }


        updateTable.setModel(new DefaultTableModel(tableContent, rows) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
    }

    public void createBox(StartPanelInfoBox box) {

        int width = 200;
        int height = 75;

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

}
