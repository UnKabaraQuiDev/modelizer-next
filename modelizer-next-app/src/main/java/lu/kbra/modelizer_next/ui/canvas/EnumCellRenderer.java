package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import lu.kbra.modelizer_next.domain.data.DisplayValueOwner;
import lu.kbra.pclib.PCUtils;

public class EnumCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 2526755178037366697L;

	@Override
	public Component getListCellRendererComponent(
			final JList<?> list,
			final Object value,
			final int index,
			final boolean isSelected,
			final boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof final DisplayValueOwner dvo) {
			this.setText(dvo.getDisplayValue());
		} else if (value instanceof final Enum<?> e) {
			this.setText(PCUtils.capitalize(e.name().toLowerCase().replace('_', ' ')));
		} else {
			this.setText(value != null ? value.toString() : "");
		}

		return this;
	}

}