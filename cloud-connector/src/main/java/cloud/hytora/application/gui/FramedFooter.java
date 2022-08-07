
package cloud.hytora.application.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import cloud.hytora.common.DriverVersion;
import cloud.hytora.driver.CloudDriver;
import com.formdev.flatlaf.*;
import cloud.hytora.application.elements.LookAndFeelsComboBox;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.*;

public class FramedFooter extends JPanel {

    protected Application frame;
    protected JTabbedPane tabbedPane;

    private final JSeparator separator1;
    private final LookAndFeelsComboBox lookAndFeelComboBox;
    private final JCheckBox lockInteraction;
    private final JLabel infoLabel;
    private final JButton closeButton;

    public FramedFooter() {

        separator1 = new JSeparator();
        lookAndFeelComboBox = new LookAndFeelsComboBox();
        lockInteraction = new JCheckBox();
        infoLabel = new JLabel();
        closeButton = new JButton();

        //======== this ========
        setLayout(new MigLayout("insets dialog", "[fill]" + "[fill]" + "[fill]" + "[grow,fill]" + "[button,fill]", "[bottom]" + "[]"));
        add(separator1, "cell 0 0 5 1");

        lookAndFeelComboBox.addActionListener(e -> lookAndFeelChanged());
        add(lookAndFeelComboBox, "cell 0 1");

        lockInteraction.setText("Lock Interaction");
        lockInteraction.setMnemonic('E');
        lockInteraction.setSelected(false);
        lockInteraction.addActionListener(e -> lockChanged());
        add(lockInteraction, "cell 2 1");

        infoLabel.setText("???");
        add(infoLabel, "cell 3 1,alignx center,growx 0");

        updateInfoLabel();

        closeButton.setText("Quit");
        closeButton.addActionListener(e -> closePerformed());
        add(closeButton, "cell 4 1");

        MigLayout layout = (MigLayout) getLayout();
        LC lc = ConstraintParser.parseLayoutConstraint((String) layout.getLayoutConstraints());
        UnitValue[] insets = lc.getInsets();
        lc.setInsets(new UnitValue[]{
                new UnitValue(0, UnitValue.PIXEL, null),
                insets[1],
                insets[2],
                insets[3]
        });
        layout.setLayoutConstraints(lc);

        DefaultComboBoxModel<LookAndFeelInfo> lafModel = new DefaultComboBoxModel<>();
        lafModel.addElement(new LookAndFeelInfo("Light Theme [F1]", FlatLightLaf.class.getName()));
        lafModel.addElement(new LookAndFeelInfo("Dark Theme [F2]", FlatDarkLaf.class.getName()));
        lafModel.addElement(new LookAndFeelInfo("Darcula Theme [F3]", FlatDarculaLaf.class.getName()));

        lookAndFeelComboBox.setModel(lafModel);

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                EventQueue.invokeLater(() -> {
                    updateInfoLabel();
                    if (frame == null) {
                        return;
                    }
                    frame.updateFontMenuItems();
                    frame.getRootPane().setDefaultButton(closeButton);
                });
            }
        });

        UIScale.addPropertyChangeListener(e -> {
            updateInfoLabel();
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (infoLabel != null)
            updateInfoLabel();
    }

    public void initialize(Application frame, JTabbedPane tabbedPane) {
        this.frame = frame;
        this.tabbedPane = tabbedPane;

        // register F1, F2, F3 keys to switch to Light, Dark
        registerSwitchToLookAndFeel(KeyEvent.VK_F1, FlatLightLaf.class.getName());
        registerSwitchToLookAndFeel(KeyEvent.VK_F2, FlatDarkLaf.class.getName());
        registerSwitchToLookAndFeel(KeyEvent.VK_F3, FlatDarculaLaf.class.getName());

        // register ESC key to close frame
        ((JComponent) frame.getContentPane()).registerKeyboardAction(
                e -> {
                    frame.dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // make the "close" button the default button
        frame.getRootPane().setDefaultButton(closeButton);

        // update info label and move focus to "close" button
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                updateInfoLabel();
                closeButton.requestFocusInWindow();
            }
        });

        // update info label when moved to another screen
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateInfoLabel();
            }
        });
    }

    private void updateInfoLabel() {
        DriverVersion version = DriverVersion.getCurrentVersion();
        infoLabel.setText("HytoraCloud Version: " + version.toString());
    }

    private void registerSwitchToLookAndFeel(int keyCode, String lafClassName) {
        ((JComponent) frame.getContentPane()).registerKeyboardAction(
                e -> {
                    selectLookAndFeel(lafClassName);
                },
                KeyStroke.getKeyStroke(keyCode, 0, false),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void selectLookAndFeel(String lafClassName) {
        lookAndFeelComboBox.setSelectedLookAndFeel(lafClassName);
    }

    private void lookAndFeelChanged() {
        String lafClassName = lookAndFeelComboBox.getSelectedLookAndFeel();
        if (lafClassName == null)
            return;

        if (lafClassName.equals(UIManager.getLookAndFeel().getClass().getName()))
            return;

        EventQueue.invokeLater(() -> {
            try {
                FlatAnimatedLafChange.showSnapshot();

                // change look and feel
                UIManager.setLookAndFeel(lafClassName);

                // clear custom default font when switching to non-FlatLaf LaF
                if (!(UIManager.getLookAndFeel() instanceof FlatLaf))
                    UIManager.put("defaultFont", null);

                // update all components
                FlatLaf.updateUI();
                FlatAnimatedLafChange.hideSnapshotWithAnimation();

                // increase size of frame if necessary
                int width = frame.getWidth();
                int height = frame.getHeight();
                Dimension prefSize = frame.getPreferredSize();
                if (prefSize.width > width || prefSize.height > height)
                    frame.setSize(Math.max(prefSize.width, width), Math.max(prefSize.height, height));

            } catch (Exception ex) {
                LoggingFacade.INSTANCE.logSevere(null, ex);
            }
        });
    }

    private void lockChanged() {
        enabledDisable(tabbedPane, !lockInteraction.isSelected());

        // repainting whole tabbed pane is faster than repainting many individual components
        tabbedPane.repaint();
    }

    private void enabledDisable(Container container, boolean enabled) {
        if (container == null) {
            return;
        }
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                enabledDisable((JPanel) c, enabled);
                continue;
            }

            c.setEnabled(enabled);

            if (c instanceof JScrollPane) {
                Component view = ((JScrollPane) c).getViewport().getView();
                if (view != null)
                    view.setEnabled(enabled);
            } else if (c instanceof JTabbedPane) {
                JTabbedPane tabPane = (JTabbedPane) c;
                int tabCount = tabPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    Component tab = tabPane.getComponentAt(i);
                    if (tab != null)
                        tab.setEnabled(enabled);
                }
            }

            if (c instanceof JToolBar)
                enabledDisable((JToolBar) c, enabled);
        }
    }

    private void closePerformed() {
        if (frame != null) {
            frame.dispose();
        }
        CloudDriver.getInstance().shutdown();
        System.exit(0);
    }

}
