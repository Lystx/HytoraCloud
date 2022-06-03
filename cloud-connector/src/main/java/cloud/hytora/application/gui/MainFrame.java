
package cloud.hytora.application.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.StyleContext;

import cloud.hytora.application.bootstrap.Bootstrap;
import cloud.hytora.application.elements.StartPanelInfoBox;
import cloud.hytora.application.gui.panels.SettingsPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import cloud.hytora.application.gui.panels.StartPanel;
import cloud.hytora.application.gui.panels.TabsPanel;
import cloud.hytora.application.elements.panel.ServicePanel;
import cloud.hytora.application.elements.panel.DataComponentsPanel;
import cloud.hytora.application.elements.panel.OptionPanePanel;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.formdev.flatlaf.ui.FlatUIUtils;
import lombok.Getter;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.*;

public class MainFrame extends JFrame {

    private final String[] availableFontFamilyNames;
    private int initialFontMenuItemCount = -1;

    private JMenuItem htmlMenuItem;
    private JMenu fontMenu;
    private JMenuItem infoItem, discordItem;
    private JToolBar toolBar;
    private JTabbedPane tabbedPane;
    private FramedFooter controlBar;

    @Getter
    private final Map<Integer, StartPanelInfoBox> boxes = new HashMap<>();

    public void registerInfoBox(StartPanelInfoBox infoBox) {
        this.boxes.put(infoBox.getId(), infoBox);
    }

    public StartPanelInfoBox getInfoBox(int id) {
        return boxes.get(id);
    }

    public MainFrame() {
        int tabIndex = Bootstrap.getState().getInt("tab", 0);

        availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().clone();
        Arrays.sort(availableFontFamilyNames);

        init();
        updateFontMenuItems();
        controlBar.initialize(this, tabbedPane);

        setIconImages(FlatSVGUtils.createWindowIconImages("/cloud/hytora/img/hytoraCloud.svg"));

        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount() && tabIndex != tabbedPane.getSelectedIndex())
            tabbedPane.setSelectedIndex(tabIndex);

        // integrate into macOS screen menu
        FlatDesktop.setAboutHandler(this::showAboutScreen);
        FlatDesktop.setPreferencesHandler(this::showPreferences);
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);

    }

    @Override
    public void dispose() {
        super.dispose();

        FlatUIDefaultsInspector.hide();
    }

    private void showAboutScreen() {
        JLabel titleLabel = new JLabel("HytoraCloud Connector Application");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");

        String link = "https://github.com/Lystx/HytoraCloud";
        JLabel linkLabel = new JLabel("<html><a href=\"#\">" + link + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException ex) {
                    JOptionPane.showMessageDialog(linkLabel, "Failed to open '" + link + "' in browser.", "About", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });


        JOptionPane.showMessageDialog(this,
                new Object[]{
                        titleLabel,
                        "An easy-to-use interface for your HytoraCloud-Node",
                        " ",
                        "Copyright 2022-" + Year.now() + " HytoraCloud",
                        linkLabel,
                },
                "Info", JOptionPane.PLAIN_MESSAGE);
    }

    private void showPreferences() {

    }

    private void selectedTabChanged() {
        Bootstrap.getState().putInt("tab", tabbedPane.getSelectedIndex());
    }

    private void menuItemActionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, e.getActionCommand(), "Menu Item", JOptionPane.PLAIN_MESSAGE);
        });
    }


    private void fontFamilyChanged(ActionEvent e) {
        String fontFamily = e.getActionCommand();

        FlatAnimatedLafChange.showSnapshot();

        Font font = UIManager.getFont("defaultFont");
        Font newFont = StyleContext.getDefaultStyleContext().getFont(fontFamily, font.getStyle(), font.getSize());
        // StyleContext.getFont() may return a UIResource, which would cause loosing user scale factor on Windows
        newFont = FlatUIUtils.nonUIResource(newFont);
        UIManager.put("defaultFont", newFont);

        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private void fontSizeChanged(ActionEvent e) {
        String fontSizeStr = e.getActionCommand();

        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont((float) Integer.parseInt(fontSizeStr));
        UIManager.put("defaultFont", newFont);

        FlatLaf.updateUI();
    }

    private void restoreFont() {
        UIManager.put("defaultFont", null);
        updateFontMenuItems();
        FlatLaf.updateUI();
    }

    private void incrFont() {
        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont((float) (font.getSize() + 1));
        UIManager.put("defaultFont", newFont);

        updateFontMenuItems();
        FlatLaf.updateUI();
    }

    private void decrFont() {
        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont((float) Math.max(font.getSize() - 1, 10));
        UIManager.put("defaultFont", newFont);

        updateFontMenuItems();
        FlatLaf.updateUI();
    }

    public void updateFontMenuItems() {
        if (initialFontMenuItemCount < 0)
            initialFontMenuItemCount = fontMenu.getItemCount();
        else {
            // remove old font items
            for (int i = fontMenu.getItemCount() - 1; i >= initialFontMenuItemCount; i--)
                fontMenu.remove(i);
        }

        // get current font
        Font currentFont = UIManager.getFont("Label.font");
        String currentFamily = currentFont.getFamily();
        String currentSize = Integer.toString(currentFont.getSize());

        // add font families
        fontMenu.addSeparator();
        ArrayList<String> families = new ArrayList<>(Arrays.asList(
                "Arial", "Cantarell", "Comic Sans MS", "Courier New", "DejaVu Sans",
                "Dialog", "Liberation Sans", "Monospaced", "Noto Sans", "Roboto",
                "SansSerif", "Segoe UI", "Serif", "Tahoma", "Ubuntu", "Verdana"));
        if (!families.contains(currentFamily))
            families.add(currentFamily);
        families.sort(String.CASE_INSENSITIVE_ORDER);

        ButtonGroup familiesGroup = new ButtonGroup();
        for (String family : families) {
            if (Arrays.binarySearch(availableFontFamilyNames, family) < 0)
                continue; // not available

            JCheckBoxMenuItem item = new JCheckBoxMenuItem(family);
            item.setSelected(family.equals(currentFamily));
            item.addActionListener(this::fontFamilyChanged);
            fontMenu.add(item);

            familiesGroup.add(item);
        }

        // add font sizes
        fontMenu.addSeparator();
        ArrayList<String> sizes = new ArrayList<>(Arrays.asList(
                "10", "11", "12", "14", "16", "18", "20", "24", "28"));
        if (!sizes.contains(currentSize))
            sizes.add(currentSize);
        sizes.sort(String.CASE_INSENSITIVE_ORDER);

        ButtonGroup sizesGroup = new ButtonGroup();
        for (String size : sizes) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(size);
            item.setSelected(size.equals(currentSize));
            item.addActionListener(this::fontSizeChanged);
            fontMenu.add(item);

            sizesGroup.add(item);
        }

        // enabled/disable items
        boolean enabled = UIManager.getLookAndFeel() instanceof FlatLaf;
        for (Component item : fontMenu.getMenuComponents())
            item.setEnabled(enabled);
    }

    public void init() {
        JMenuBar menuBar1 = new JMenuBar();
        JMenu viewMenu = new JMenu();
        JCheckBoxMenuItem checkBoxMenuItem1 = new JCheckBoxMenuItem();
        JMenu menu1 = new JMenu();
        JMenu subViewsMenu = new JMenu();
        JMenu subSubViewsMenu = new JMenu();
        JMenuItem errorLogViewMenuItem = new JMenuItem();
        JMenuItem searchViewMenuItem = new JMenuItem();
        JMenuItem projectViewMenuItem = new JMenuItem();
        JMenuItem structureViewMenuItem = new JMenuItem();
        JMenuItem propertiesViewMenuItem = new JMenuItem();
        JMenuItem menuItem2 = new JMenuItem();
        htmlMenuItem = new JMenuItem();
        JRadioButtonMenuItem radioButtonMenuItem1 = new JRadioButtonMenuItem();
        JRadioButtonMenuItem radioButtonMenuItem2 = new JRadioButtonMenuItem();
        JRadioButtonMenuItem radioButtonMenuItem3 = new JRadioButtonMenuItem();
        fontMenu = new JMenu();
        JMenuItem restoreFontMenuItem = new JMenuItem();
        JMenuItem incrFontMenuItem = new JMenuItem();
        JMenuItem decrFontMenuItem = new JMenuItem();
        JMenu helpMenu = new JMenu();
        infoItem = new JMenuItem();
        discordItem = new JMenuItem();
        toolBar = new JToolBar();
        JButton refreshButton = new JButton();
        JPanel contentPanel = new JPanel();
        tabbedPane = new JTabbedPane();
        ServicePanel servicePanel = new ServicePanel();
        StartPanel startPanel = new StartPanel(this);
        DataComponentsPanel dataComponentsPanel = new DataComponentsPanel();
        TabsPanel tabsPanel = new TabsPanel();
        OptionPanePanel optionPanePanel = new OptionPanePanel();
        SettingsPanel settingsPanel1 = new SettingsPanel();
        controlBar = new FramedFooter();

        //======== this ========
        setTitle("HytoraCloud | Application");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== viewMenu ========
            {
                viewMenu.setText("View");
                viewMenu.setMnemonic('V');

                //---- checkBoxMenuItem1 ----
                checkBoxMenuItem1.setText("Show Toolbar");
                checkBoxMenuItem1.setSelected(true);
                checkBoxMenuItem1.setMnemonic('T');
                checkBoxMenuItem1.addActionListener(e -> menuItemActionPerformed(e));
                viewMenu.add(checkBoxMenuItem1);

                //======== menu1 ========
                {
                    menu1.setText("Show View");
                    menu1.setMnemonic('V');

                    //======== subViewsMenu ========
                    {
                        subViewsMenu.setText("Sub Views");
                        subViewsMenu.setMnemonic('S');

                        //======== subSubViewsMenu ========
                        {
                            subSubViewsMenu.setText("Sub sub Views");
                            subSubViewsMenu.setMnemonic('U');

                            //---- errorLogViewMenuItem ----
                            errorLogViewMenuItem.setText("Error Log");
                            errorLogViewMenuItem.setMnemonic('E');
                            errorLogViewMenuItem.addActionListener(e -> menuItemActionPerformed(e));
                            subSubViewsMenu.add(errorLogViewMenuItem);
                        }
                        subViewsMenu.add(subSubViewsMenu);

                        //---- searchViewMenuItem ----
                        searchViewMenuItem.setText("Search");
                        searchViewMenuItem.setMnemonic('S');
                        searchViewMenuItem.addActionListener(e -> menuItemActionPerformed(e));
                        subViewsMenu.add(searchViewMenuItem);
                    }
                    menu1.add(subViewsMenu);

                    //---- projectViewMenuItem ----
                    projectViewMenuItem.setText("Project");
                    projectViewMenuItem.setMnemonic('P');
                    projectViewMenuItem.addActionListener(e -> menuItemActionPerformed(e));
                    menu1.add(projectViewMenuItem);

                    //---- structureViewMenuItem ----
                    structureViewMenuItem.setText("Structure");
                    structureViewMenuItem.setMnemonic('T');
                    structureViewMenuItem.addActionListener(e -> menuItemActionPerformed(e));
                    menu1.add(structureViewMenuItem);

                    //---- propertiesViewMenuItem ----
                    propertiesViewMenuItem.setText("Properties");
                    propertiesViewMenuItem.setMnemonic('O');
                    propertiesViewMenuItem.addActionListener(e -> menuItemActionPerformed(e));
                    menu1.add(propertiesViewMenuItem);
                }
                viewMenu.add(menu1);

                //---- menuItem2 ----
                menuItem2.setText("Disabled Item");
                menuItem2.setEnabled(false);
                viewMenu.add(menuItem2);

                //---- htmlMenuItem ----
                htmlMenuItem.setText("<html>some <b color=\"red\">HTML</b> <i color=\"blue\">text</i></html>");
                viewMenu.add(htmlMenuItem);
                viewMenu.addSeparator();

                //---- radioButtonMenuItem1 ----
                radioButtonMenuItem1.setText("Details");
                radioButtonMenuItem1.setSelected(true);
                radioButtonMenuItem1.setMnemonic('D');
                radioButtonMenuItem1.addActionListener(e -> menuItemActionPerformed(e));
                viewMenu.add(radioButtonMenuItem1);

                //---- radioButtonMenuItem2 ----
                radioButtonMenuItem2.setText("Small Icons");
                radioButtonMenuItem2.setMnemonic('S');
                radioButtonMenuItem2.addActionListener(e -> menuItemActionPerformed(e));
                viewMenu.add(radioButtonMenuItem2);

                //---- radioButtonMenuItem3 ----
                radioButtonMenuItem3.setText("Large Icons");
                radioButtonMenuItem3.setMnemonic('L');
                radioButtonMenuItem3.addActionListener(e -> menuItemActionPerformed(e));
                viewMenu.add(radioButtonMenuItem3);
            }
            menuBar1.add(viewMenu);

            //======== fontMenu ========
            {
                fontMenu.setText("Font");

                //---- restoreFontMenuItem ----
                restoreFontMenuItem.setText("Restore Font");
                restoreFontMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                restoreFontMenuItem.addActionListener(e -> restoreFont());
                fontMenu.add(restoreFontMenuItem);

                //---- incrFontMenuItem ----
                incrFontMenuItem.setText("Increase Font Size");
                incrFontMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                incrFontMenuItem.addActionListener(e -> incrFont());
                fontMenu.add(incrFontMenuItem);

                //---- decrFontMenuItem ----
                decrFontMenuItem.setText("Decrease Font Size");
                decrFontMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                decrFontMenuItem.addActionListener(e -> decrFont());
                fontMenu.add(decrFontMenuItem);
            }
            menuBar1.add(fontMenu);

            {
                helpMenu.setText("Help");
                helpMenu.setMnemonic('H');

                infoItem.setText("Info");
                infoItem.setMnemonic('I');
                infoItem.addActionListener(e -> showAboutScreen());
                helpMenu.add(infoItem);


                discordItem.setText("Discord");
                discordItem.setMnemonic('D');
                discordItem.addActionListener(e -> {
                    String link = "https://discord.gg/pazzqaGSVs";
                    try {
                        Desktop.getDesktop().browse(new URI(link));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                });
                helpMenu.add(discordItem);
            }
            menuBar1.add(helpMenu);
        }
        setJMenuBar(menuBar1);

        {
            toolBar.setMargin(new Insets(3, 3, 3, 3));

            //---- refreshButton ----
            refreshButton.setToolTipText("Refresh all cloud data");
            toolBar.add(refreshButton);
            toolBar.addSeparator();

        }
        contentPane.add(toolBar, BorderLayout.NORTH);

        //======== contentPanel ========
        {
            contentPanel.setLayout(new MigLayout(
                    "insets dialog,hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[grow,fill]"));

            {
                tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                tabbedPane.addChangeListener(e -> selectedTabChanged());
                tabbedPane.addTab("Start", startPanel);
                tabbedPane.addTab("Services", servicePanel);
                tabbedPane.addTab("Nodes", dataComponentsPanel);
                tabbedPane.addTab("Players", tabsPanel);
                tabbedPane.addTab("Modules", optionPanePanel);
                tabbedPane.addTab("Settings", settingsPanel1);
            }
            contentPanel.add(tabbedPane, "cell 0 0");
        }
        contentPane.add(contentPanel, BorderLayout.CENTER);
        contentPane.add(controlBar, BorderLayout.SOUTH);

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioButtonMenuItem1);
        buttonGroup1.add(radioButtonMenuItem2);
        buttonGroup1.add(radioButtonMenuItem3);


        // add "Users" button to menubar
        FlatButton usersButton = new FlatButton();
        usersButton.setIcon(new FlatSVGIcon("cloud/hytora/img/users.svg"));
        usersButton.setButtonType(ButtonType.toolBarButton);
        usersButton.setFocusable(false);
        usersButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Account managing is coming soon!", "HytoraCloud | Login", JOptionPane.INFORMATION_MESSAGE));
        menuBar1.add(Box.createGlue());
        menuBar1.add(usersButton);

        refreshButton.setIcon(new FlatSVGIcon("cloud/hytora/img/refresh.svg"));


        // remove contentPanel bottom insets
        MigLayout layout = (MigLayout) contentPanel.getLayout();
        LC lc = ConstraintParser.parseLayoutConstraint((String) layout.getLayoutConstraints());
        UnitValue[] insets = lc.getInsets();
        lc.setInsets(new UnitValue[]{
                insets[0],
                insets[1],
                new UnitValue(0, UnitValue.PIXEL, null),
                insets[3]
        });
        layout.setLayoutConstraints(lc);
    }

}
