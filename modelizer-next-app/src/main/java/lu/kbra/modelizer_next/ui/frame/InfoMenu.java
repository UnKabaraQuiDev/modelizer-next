package lu.kbra.modelizer_next.ui.frame;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import lu.kbra.modelizer_next.App;
import lu.kbra.modelizer_next.bootstrap.BootstrapConfig;
import lu.kbra.modelizer_next.bootstrap.UpdateChannel;
import lu.kbra.modelizer_next.bootstrap.UpdateRuntime;
import lu.kbra.modelizer_next.ui.help.HelpDialog;
import lu.kbra.pclib.PCUtils;

final class InfoMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	InfoMenu(final MainFrame frame) {
		super("Info");
		this.add(this.createHelpItem());
		this.add(this.createAutoUpdateItem(frame));
		this.add(this.createCheckForUpdatesItem(frame));
		this.add(this.createUpdateChannelMenu(frame));
		this.add(this.createVersionInfoItem(frame));
		this.addBootstrapVersionInfoIfAvailable(frame);
		this.addBootstrapCleanupIfAvailable(frame);
//		this.add(this.createOpenUrlItem("Report issue...",
//				App.ISSUES_URL,
//				"Report an issue",
//				"The issue link has been copied to your clipboard:"));
//		this.add(this
//				.createOpenUrlItem("Website...", App.WEBSITE_URL, "Visit website", "The website link has been copied to your clipboard:"));
	}

	private void addBootstrapCleanupIfAvailable(final MainFrame frame) {
		final Optional<UpdateRuntime> bootstrapRuntime = frame.bootstrapRuntime();
		if (bootstrapRuntime.isEmpty()) {
			return;
		}

		final JMenuItem bootstrapSpaceInfo = new JMenuItem("Used space: unknown");
		bootstrapSpaceInfo.setToolTipText("Clear to remove unused application version files.");
		final Runnable recomputeBoostrapSpaceUsage = () -> {
			bootstrapSpaceInfo.setText("Used space: computing...");
			try {
				bootstrapSpaceInfo.setText(
						"Used space: " + PCUtils.getHumanFormatFileSize(bootstrapRuntime.get().getInstalledUpdatesDiskUsageBytes()));
			} catch (final IOException e) {
				bootstrapSpaceInfo.setText("Used space: An error occured (" + e.getMessage() + ")");
			}
		};
		recomputeBoostrapSpaceUsage.run();
		bootstrapSpaceInfo.addActionListener(event -> {
			try {
				final long size = bootstrapRuntime.get().freeUnusedInstalledUpdates();
				JOptionPane.showMessageDialog(frame,
						"Freed " + PCUtils.getHumanFormatFileSize(size) + " from disk.",
						"Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(frame,
						"An error occured while freeing disk space:\n" + e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
			recomputeBoostrapSpaceUsage.run();
		});
		this.add(bootstrapSpaceInfo);
	}

	private void addBootstrapVersionInfoIfAvailable(final MainFrame frame) {
		final Optional<UpdateRuntime> bootstrapRuntime = frame.bootstrapRuntime();
		if (bootstrapRuntime.isEmpty()) {
			return;
		}

		final BootstrapConfig bootstrapConfig = bootstrapRuntime.get().getBootstrapConfig();
		final JMenuItem bootstrapVersionInfo = new JMenuItem(
				"Bootstrap Version: " + bootstrapConfig.version() + " [" + bootstrapConfig.distributor() + "]");
		bootstrapVersionInfo.setToolTipText("Click to copy bootstrap version informations.");
		bootstrapVersionInfo
				.addActionListener(
						event -> Toolkit.getDefaultToolkit()
								.getSystemClipboard()
								.setContents(
										new StringSelection(
												"==== APP INFO ====\n" + App.JSON.toPrettyString() + "\n==== BOOTSTRAP INFO ====\n"
														+ bootstrapRuntime.get().getBootstrapJson().toPrettyString()),
										null));
		this.add(bootstrapVersionInfo);
	}

	private JCheckBoxMenuItem createAutoUpdateItem(final MainFrame frame) {
		final Optional<UpdateRuntime> bootstrapRuntime = frame.bootstrapRuntime();
		final boolean updateRuntimeAvailable = bootstrapRuntime.isPresent();
		final JCheckBoxMenuItem autoCheckUpdates = new JCheckBoxMenuItem("Check for updates on startup",
				updateRuntimeAvailable && bootstrapRuntime.get().isAutoCheckUpdates());
		autoCheckUpdates.setEnabled(updateRuntimeAvailable && bootstrapRuntime.get().isAutomaticUpdateChecksEnabledByProperty());
		autoCheckUpdates
				.addActionListener(event -> frame.bootstrapRuntime().ifPresent(c -> c.setAutoCheckUpdates(autoCheckUpdates.isSelected())));
		return autoCheckUpdates;
	}

	private JMenuItem createCheckForUpdatesItem(final MainFrame frame) {
		final JMenuItem checkForUpdates = new JMenuItem("Check for updates...");
		checkForUpdates.addActionListener(event -> frame.checkForUpdatesManually());
		return checkForUpdates;
	}

	private JMenuItem createHelpItem() {
		final JMenuItem checkForUpdates = new JMenuItem("Help...");
		checkForUpdates.addActionListener(event -> new HelpDialog().setVisible(true));
		return checkForUpdates;
	}

	private JMenuItem createOpenUrlItem(final String text, final String url, final String title, final String fallbackMessage) {
		final JMenuItem item = new JMenuItem(text);
		item.addActionListener(action -> {
			try {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
					Desktop.getDesktop().browse(new URI(url));
					return;
				}
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
			JOptionPane.showMessageDialog(null, fallbackMessage + "\n" + url, title, JOptionPane.INFORMATION_MESSAGE);
		});
		return item;
	}

	private JMenu createUpdateChannelMenu(final MainFrame frame) {
		final Optional<UpdateRuntime> bootstrapRuntime = frame.bootstrapRuntime();
		final boolean updateRuntimeAvailable = bootstrapRuntime.isPresent();
		final JMenu channelMenu = new JMenu("Update channel");
		channelMenu.setEnabled(updateRuntimeAvailable);
		final ButtonGroup channelGroup = new ButtonGroup();
		final UpdateChannel selectedChannel = updateRuntimeAvailable ? bootstrapRuntime.get().getSelectedChannel() : UpdateChannel.RELEASE;
		for (final UpdateChannel updateChannel : UpdateChannel.values()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(updateChannel.displayName());
			item.setSelected(updateChannel == selectedChannel);
			item.addActionListener(event -> frame.bootstrapRuntime().ifPresent(c -> {
				c.setSelectedChannel(updateChannel);
				frame.checkForUpdatesManually();
			}));
			channelGroup.add(item);
			channelMenu.add(item);
		}
		return channelMenu;
	}

	private JMenuItem createVersionInfoItem(final MainFrame frame) {
		final JMenuItem versionInfo = new JMenuItem("Version: " + App.VERSION + " [" + App.DISTRIBUTOR + "]");
		versionInfo.setToolTipText("Click to copy version informations.");
		final Optional<UpdateRuntime> bootstrapRuntime = frame.bootstrapRuntime();
		final boolean updateRuntimeAvailable = bootstrapRuntime.isPresent();
		versionInfo
				.addActionListener(event -> Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.setContents(
								new StringSelection("==== APP INFO ====\n" + App.JSON.toPrettyString() + "\n==== BOOTSTRAP INFO ====\n"
										+ (updateRuntimeAvailable ? bootstrapRuntime.get().getBootstrapJson().toPrettyString() : "NONE")),
								null));
		return versionInfo;
	}

}
