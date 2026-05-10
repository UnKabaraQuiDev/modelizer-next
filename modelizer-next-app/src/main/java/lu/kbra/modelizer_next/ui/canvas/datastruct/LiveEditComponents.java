package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lu.kbra.modelizer_next.style.StylePalette;

/**
 * Swing components used during inline editing on the canvas.
 * @param textField text field value used by the operation
 * @param textArea text area value used by the operation
 * @param enumComboBox enum combo box value used by the operation
 * @param paletteList list to read or update
 * @param enumList list to read or update
 */
public record LiveEditComponents(JTextField textField, JTextArea textArea, @Deprecated
JComboBox<Enum<?>> enumComboBox, JList<StylePalette> paletteList, JList<Enum<?>> enumList) {

	/**
	 * Applies the supplied consumer to each live edit Swing component.
	 * @param consumer consumer value used by the operation
	 */
	public void forEach(final Consumer<JComponent> consumer) {
		consumer.accept(this.textField);
		consumer.accept(this.textArea);
		consumer.accept(this.enumComboBox);
		consumer.accept(this.paletteList);
		consumer.accept(this.enumList);
	}

	/**
	 * Sets the visible on the active canvas.
	 * @param b whether b is enabled
	 */
	public void setVisible(final boolean b) {
		this.forEach(c -> c.setVisible(b));
	}

}
