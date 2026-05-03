package lu.kbra.modelizer_next.ui.help;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.common.SystemThemeDetector;
import lu.kbra.modelizer_next.ui.frame.MainFrame;

public class HelpDialog extends JFrame {

	private static final long serialVersionUID = -2242189520928100036L;

	private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(560, 420);
	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(980, 720);

	private final ShortcutsTab shortcutsTab = new ShortcutsTab();

	public HelpDialog() {
		super(App.title("Help"));
		super.setIconImage(MainFrame.ICON);
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setLayout(new BorderLayout());

		super.add(this.createTabs(), BorderLayout.CENTER);
		super.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(final WindowEvent event) {
				HelpDialog.this.shortcutsTab.registerKeyDispatcher();
			}

			@Override
			public void windowClosed(final WindowEvent event) {
				HelpDialog.this.shortcutsTab.unregisterKeyDispatcher();
			}
		});

		super.setMinimumSize(MINIMUM_WINDOW_SIZE);
		super.setSize(DEFAULT_WINDOW_SIZE);
		super.setLocationRelativeTo(null);
		super.setResizable(true);
	}

	@Override
	public void dispose() {
		this.shortcutsTab.unregisterKeyDispatcher();
		super.dispose();
	}

	private JTabbedPane createTabs() {
		final JTabbedPane tabs = new JTabbedPane();
		tabs.putClientProperty(FlatClientProperties.STYLE, "tabType: card");

		tabs.addTab("Info", new InfoTab());
		tabs.addTab("Shortcuts", this.shortcutsTab);

		return tabs;
	}

	public static void main(final String[] args) {
		if (SystemThemeDetector.isDark()) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}

		final HelpDialog dialog = new HelpDialog();
		dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		dialog.setVisible(true);
	}

}
