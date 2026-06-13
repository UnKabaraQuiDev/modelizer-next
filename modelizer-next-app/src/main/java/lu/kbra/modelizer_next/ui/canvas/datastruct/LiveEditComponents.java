package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.CopyPastePopupMenu;
import lu.kbra.modelizer_next.ui.canvas.FieldTagsPopupMenu;

/**
 * Swing components used during live editing on the canvas.
 */
public record LiveEditComponents(
		JTextField textField,
		JTextArea textArea,
		@Deprecated JComboBox<Enum<?>> enumComboBox,
		JList<StylePalette> paletteList,
		JList<Enum<?>> enumList,
		CopyPastePopupMenu copyPastePopupMenu,
		FieldTagsPopupMenu fieldTagsPopupMenu) {

	/**
	 * Applies the supplied consumer to each live edit Swing component.
	 *
	 * @param consumer consumer value used by the operation
	 */
	public void forEach(final Consumer<JComponent> consumer) {
		consumer.accept(this.textField);
		consumer.accept(this.textArea);
		consumer.accept(this.enumComboBox);
		consumer.accept(this.paletteList);
		consumer.accept(this.enumList);
		consumer.accept(this.copyPastePopupMenu);
		consumer.accept(this.fieldTagsPopupMenu);
	}

	/**
	 * Sets the visible on the active canvas.
	 *
	 * @param b whether b is enabled
	 */
	public void setVisible(final boolean b) {
		this.forEach(c -> c.setVisible(b));
	}

}
