package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.data.StyleScope;

/**
 * Swing list renderer for style palette previews.
 */
public class StylePaletteCellRenderer extends JPanel implements ListCellRenderer<StylePalette> {

	private static final long serialVersionUID = 3461088611588880072L;

	private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(4, 8, 4, 8);

	private final JLabel label = new JLabel();

	private StyleScope scope = StyleScope.CLASS;

	/**
	 * Creates a style palette cell renderer instance.
	 */
	public StylePaletteCellRenderer() {
		this.setLayout(new BorderLayout());
		this.add(this.label, BorderLayout.CENTER);

		this.label.setOpaque(false);

		this.setBorder(StylePaletteCellRenderer.EMPTY_BORDER);
		this.setOpaque(true);
	}

	/**
	 * Sets the scope on the active canvas.
	 *
	 * @param scope export scope to use
	 */
	public void setScope(final StyleScope scope) {
		this.scope = scope;
	}

	/**
	 * Returns the list cell renderer component.
	 *
	 * @param list         list to read or update
	 * @param value        value to process
	 * @param index        zero-based index to read or update
	 * @param isSelected   whether is selected is enabled
	 * @param cellHasFocus whether cell has focus is enabled
	 * @return the list cell renderer component
	 */
	@Override
	public Component getListCellRendererComponent(
			final JList<? extends StylePalette> list,
			final StylePalette value,
			final int index,
			final boolean isSelected,
			final boolean cellHasFocus) {

		this.label.setFont(list.getFont());
		this.setFont(list.getFont());

		if (value == null) {
			this.label.setText("");
			return this;
		}

		this.label.setText(value.getName());

		switch (this.scope) {

		case CLASS -> {
			this.label.setForeground(value.getClassTextColor());
			this.setBackground(value.getClassBackgroundColor());
			this.setBorder(BorderFactory.createLineBorder(value.getClassBorderColor()));
		}

		case FIELD -> {
			this.label.setForeground(value.getFieldTextColor());
			this.setBackground(value.getFieldBackgroundColor());
			this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}

		case COMMENT -> {
			this.label.setForeground(value.getCommentTextColor());
			this.setBackground(value.getCommentBackgroundColor());
			this.setBorder(BorderFactory.createLineBorder(value.getCommentBorderColor()));
		}

		case LINK -> {
			SwingUtilities.updateComponentTreeUI(this);
			this.label.setForeground(value.getLinkColor());
			this.setBorder(BorderFactory.createLineBorder(value.getLinkColor()));
		}
		}

		this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(), StylePaletteCellRenderer.EMPTY_BORDER));

		if (isSelected) {
			this.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(list.getSelectionBackground(), 2), this.getBorder()));
		}

		return this;
	}

}
