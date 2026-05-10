package lu.kbra.modelizer_next.bootstrap.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Small Swing progress window shown by the native bootstrapper.
 */
public final class BootstrapLoadingFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final JLabel messageLabel = new JLabel("Preparing Modelizer Next...", SwingConstants.CENTER);
	private final JProgressBar progressBar = new JProgressBar();

	/**
	 * Creates a bootstrap loading frame instance.
	 */
	public BootstrapLoadingFrame() {
		super("Modelizer Next");
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setUndecorated(false);
		this.setResizable(false);
		final JPanel panel = new JPanel(new BorderLayout(0, 12));
		panel.setBorder(new EmptyBorder(18, 18, 18, 18));
		panel.add(this.messageLabel, BorderLayout.CENTER);
		panel.add(this.progressBar, BorderLayout.SOUTH);
		this.add(panel);
		this.setPreferredSize(new Dimension(420, 120));
		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * Updates the loading frame progress display.
	 *
	 * @param message message shown to the caller or user
	 * @param value   value to process
	 * @param max     numeric max value
	 */
	public void update(final String message, final int value, final int max) {
		SwingUtilities.invokeLater(() -> {
			this.messageLabel.setText(message == null || message.isBlank() ? "Preparing Modelizer Next..." : message);
			if (max <= 0) {
				this.progressBar.setIndeterminate(true);
			} else {
				this.progressBar.setIndeterminate(false);
				this.progressBar.setMinimum(0);
				this.progressBar.setMaximum(max);
				this.progressBar.setValue(Math.max(0, Math.min(value, max)));
			}
		});
	}
}
