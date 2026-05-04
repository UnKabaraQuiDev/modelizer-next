package lu.kbra.modelizer_next.ui.canvas.datastruct;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;

import lu.kbra.modelizer_next.domain.data.DisplayValueOwner;

public record RenamingComponents(JTextField textField, JTextArea textArea, JComboBox<String> comboBox) {

	public void forEach(final Consumer<JComponent> consumer) {
		consumer.accept(this.textField);
		consumer.accept(this.textArea);
		consumer.accept(this.comboBox);
	}

	public void setVisible(final boolean b) {
		this.forEach(c -> c.setVisible(b));
	}

	public void setBounds(final int x, final int y, final int x2, final int y2) {
		this.textField.setBounds(x, y, x2, y2);
		this.textArea.setBounds(x, y, x2, y2);
		this.comboBox.setBounds(x, y, x2, y2);
	}

	public void setBackground(final Color backgroundColor) {
		this.textField.setBackground(backgroundColor);
		this.textArea.setBackground(backgroundColor);
		this.comboBox.setBackground(backgroundColor);
	}

	public void setForeground(final Color textColor) {
		this.textField.setForeground(textColor);
		this.textArea.setForeground(textColor);
		this.comboBox.setForeground(textColor);
	}

	public void setBorder(final CompoundBorder compoundBorder) {
		this.textField.setBorder(compoundBorder);
		this.textArea.setBorder(compoundBorder);
		this.comboBox.setBorder(compoundBorder);
	}

	public void selectAll() {
		this.textField.selectAll();
		this.textArea.selectAll();
	}

	public void setText(final String currentValue) {
		this.textField.setText(currentValue);
		this.textArea.setText(currentValue);
	}

	public void setValues(final Class<? extends Enum<?>> enumClass, final String currentValue) {
		comboBox.setModel(new DefaultComboBoxModel<String>(Arrays.asList(enumClass.getEnumConstants())
				.stream()
				.map(c -> c instanceof DisplayValueOwner dvo ? dvo.getDisplayValue() : c.name())
				.toArray(String[]::new)));
	}

}
