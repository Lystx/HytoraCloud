
package cloud.hytora.application.gui.panels;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.driver.CloudDriver;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;
import net.miginfocom.swing.*;

public class SettingsPanel extends JPanel {

	private final JLabel loggerLabel;
	private final FlatTriStateCheckBox loggerBox;
	private final JLabel boxLabel;
	private final JLabel label2;
	private final JLabel label3;
	private final JSeparator separator1;

	private final JCheckBox toggleUniqueColoredTitleBar, showWindowTitleBarIcon, fadingThemeChange, menuBar;

	public SettingsPanel() {
		loggerLabel = new JLabel();
		loggerBox = new FlatTriStateCheckBox();
		boxLabel = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		separator1 = new JSeparator();

		//======== this ========
		setLayout(new MigLayout(
				"insets dialog,hidemode 3",
				// columns
				"[]" +
						"[]" +
						"[left]",
				// rows
				"[]para" +
						"[]" +
						"[]" +
						"[]" +
						"[]" +
						"[]" +
						"[]" +
						"[]"));


		loggerLabel.setText("Logger:");
		add(loggerLabel, "cell 0 1");

		loggerBox.setText("Level => ");
		loggerBox.addActionListener(e -> loggerBoxChanged());
		add(loggerBox, "cell 1 1");

		boxLabel.setText("text");
		boxLabel.setEnabled(false);
		add(boxLabel, "cell 2 1,");
		boxLabel.setText("TRACE");

		//---- label2 ----
		label2.setText("Appearance settings:");
		add(label2, "cell 0 2");

		//---- label3 ----
		label3.setText("Here you can customize your appearance settings");
		add(label3, "cell 1 3 2 1");
		add(separator1, "cell 1 4 2 1,growx");

		toggleUniqueColoredTitleBar = new JCheckBox("Unique colored title bar");
		toggleUniqueColoredTitleBar.setToolTipText("If the title bar should be in a unique color depending on the theme you chose");
		toggleUniqueColoredTitleBar.addActionListener(e -> {
			UIManager.put("cloud.settings.unifiedBackground", toggleUniqueColoredTitleBar.isSelected());
			FlatLaf.repaintAllFramesAndDialogs();
		});
		add(toggleUniqueColoredTitleBar, "cell 1 6");

		showWindowTitleBarIcon = new JCheckBox("Show window title bar icon");
		showWindowTitleBarIcon.setToolTipText("If icon of the application should be shown or not");
		showWindowTitleBarIcon.addActionListener(e -> {

			boolean showIcon = showWindowTitleBarIcon.isSelected();

			// for main frame (because already created)
			getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, showIcon);

			// for other not yet created frames/dialogs
			UIManager.put("cloud.settings.showIcon", showIcon);
		});
		add(showWindowTitleBarIcon, "cell 1 8");

		fadingThemeChange = new JCheckBox("Fading theme change");
		fadingThemeChange.setToolTipText("If themes should fade in when chaning");
		fadingThemeChange.addActionListener(e -> {
			System.setProperty("cloud.animatedLafChange", String.valueOf(fadingThemeChange.isSelected()));
		});
		add(fadingThemeChange, "cell 3 6");

		menuBar = new JCheckBox("Embed title bar");
		menuBar.setToolTipText("If the title bar should be embed (more modern) or not");
		menuBar.addActionListener(e -> {
			UIManager.put("cloud.settings.menuBarEmbedded", menuBar.isSelected());
			FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
		});
		add(menuBar, "cell 3 8");

		menuBar.setSelected(UIManager.getBoolean("cloud.settings.menuBarEmbedded"));
		fadingThemeChange.setSelected(UIManager.getBoolean("cloud.animatedLafChange"));
		toggleUniqueColoredTitleBar.setSelected(UIManager.getBoolean("cloud.settings.unifiedBackground"));
		showWindowTitleBarIcon.setSelected(UIManager.getBoolean("cloud.settings.showIcon"));
	}

	private void loggerBoxChanged() {
		switch (boxLabel.getText()) {
			case "TRACE":
				boxLabel.setText("DEBUG");
				CloudDriver.getInstance().getLogger().setMinLevel(LogLevel.DEBUG);
				break;
			case "DEBUG":
				boxLabel.setText("INFO");
				CloudDriver.getInstance().getLogger().setMinLevel(LogLevel.INFO);
				break;
			case "INFO":
				boxLabel.setText("TRACE");
				CloudDriver.getInstance().getLogger().setMinLevel(LogLevel.TRACE);
				break;
		}
	}

}
