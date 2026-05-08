package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import lu.kbra.modelizer_next.style.StylePalette;

public class StylePaletteRenderer extends JPanel implements ListCellRenderer<StylePalette> {

	private final JLabel label = new JLabel();

	private StyleScope scope = StyleScope.CLASS;

	public StylePaletteRenderer() {
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);

		label.setOpaque(false);

		setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		setOpaque(true);
	}

	public void setScope(final StyleScope scope) {
		this.scope = scope;
	}

	@Override
	public Component getListCellRendererComponent(
			final JList<? extends StylePalette> list,
			final StylePalette value,
			final int index,
			final boolean isSelected,
			final boolean cellHasFocus) {

		if (value == null) {
			label.setText("");
			return this;
		}

		label.setText(value.getName());

		switch (scope) {

		case CLASS -> {
			label.setForeground(value.getClassTextColor());
			setBackground(value.getClassBackgroundColor());
			setBorder(BorderFactory.createLineBorder(value.getClassBorderColor()));
		}

		case FIELD -> {
			label.setForeground(value.getFieldTextColor());
			setBackground(value.getFieldBackgroundColor());
			setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}

		case COMMENT -> {
			label.setForeground(value.getCommentTextColor());
			setBackground(value.getCommentBackgroundColor());
			setBorder(BorderFactory.createLineBorder(value.getCommentBorderColor()));
		}

		case LINK -> {
			SwingUtilities.updateComponentTreeUI(label);
			label.setForeground(value.getLinkColor());
			setBorder(BorderFactory.createLineBorder(value.getLinkColor()));
		}
		}

		if (isSelected) {
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(list.getSelectionBackground(), 2), getBorder()));
		}

		return this;
	}
}