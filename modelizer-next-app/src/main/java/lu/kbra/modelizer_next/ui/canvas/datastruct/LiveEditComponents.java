package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lu.kbra.modelizer_next.style.StylePalette;

public record LiveEditComponents(JTextField textField, JTextArea textArea, JComboBox<Enum<?>> enumComboBox,
		JList<StylePalette> paletteList) {

	public void forEach(final Consumer<JComponent> consumer) {
		consumer.accept(this.textField);
		consumer.accept(this.textArea);
		consumer.accept(this.enumComboBox);
		consumer.accept(this.paletteList);
	}

	public void setVisible(final boolean b) {
		this.forEach(c -> c.setVisible(b));
	}

}
