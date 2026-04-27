package lu.kbra.modelizer_next.ui.frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import lu.kbra.modelizer_next.App;

public interface MainFrameWindowController {

	default JLabel createStatusLabel() {
		return new JLabel("Left drag: move object   |   Middle drag: pan   |   Mouse wheel: zoom   |   Right drag: create link",
				SwingConstants.LEFT);
	}

	default JLabel createSelectionPathLabel() {
		return new JLabel("No selection", SwingConstants.RIGHT);
	}

	default JPanel createPinnedStylesPanel() {
		final MainFrame frame = (MainFrame) this;
		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
		panel.putClientProperty("dragListener", new DragListener(panel));
		panel.putClientProperty("savePalettes", (Runnable) () -> {
			final List<String> pinnedPalettes = frame.appConfig.getPinnedPaletteNames();
			pinnedPalettes.clear();
			pinnedPalettes.addAll(Arrays.stream(panel.getComponents())
					.filter(JButton.class::isInstance)
					.map(JButton.class::cast)
					.map(JButton::getText)
					.toList());
			App.saveConfig(frame.appConfig);
		});
		panel.setOpaque(false);
		return panel;
	}

	default JPanel createStatusPanel() {
		final MainFrame frame = (MainFrame) this;
		final JPanel statusPanel = new JPanel(new BorderLayout(12, 0));
		statusPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
		statusPanel.add(frame.statusLabel, BorderLayout.WEST);
		statusPanel.add(frame.selectionPathLabel, BorderLayout.EAST);
		return statusPanel;
	}

}
