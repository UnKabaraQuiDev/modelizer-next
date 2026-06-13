package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.border.LineBorder;

import lu.kbra.modelizer_next.ui.canvas.datastruct.FieldTags;

public class FieldTagsPopupMenu extends LivePopupMenu {

	private final Consumer<FieldTags> confirm;

	private final JCheckBoxMenuItem primaryKey;
	private final JCheckBoxMenuItem unique;
	private final JCheckBoxMenuItem nonNull;
	private final JMenuItem actionItem;

	public FieldTagsPopupMenu(final Consumer<FieldTags> confirm) {
		super.setBorder(new LineBorder(Color.BLACK, 1));
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.confirm = confirm;

		this.primaryKey = new JCheckBoxMenuItem("Primary key", false);
		this.unique = new JCheckBoxMenuItem("Unique", false);
		this.nonNull = new JCheckBoxMenuItem("Non null", true);

		this.actionItem = new JMenuItem("Apply");
		this.actionItem.addActionListener(this::invokeConfirm);

		super.add(this.primaryKey);
		super.add(this.unique);
		super.add(this.nonNull);
		super.add(this.actionItem);
	}

	public void apply(final FieldTags data) {
		this.primaryKey.setSelected(data.isPrimaryKey());
		this.unique.setSelected(data.isUnique());
		this.nonNull.setSelected(data.isNonNull());
	}

	public JCheckBoxMenuItem getPrimaryKey() {
		return primaryKey;
	}

	public JCheckBoxMenuItem getUnique() {
		return unique;
	}

	public JCheckBoxMenuItem getNonNull() {
		return nonNull;
	}

	public JMenuItem getActionItem() {
		return this.actionItem;
	}

	public boolean isPrimaryKey() {
		return primaryKey.isSelected();
	}

	public boolean isUnique() {
		return unique.isSelected();
	}

	public boolean isNonNull() {
		return nonNull.isSelected();
	}

	@Override
	public void invokeConfirm(final ActionEvent e) {
		this.confirm.accept(new FieldTags(isPrimaryKey(), isUnique(), isNonNull()));
	}

}
