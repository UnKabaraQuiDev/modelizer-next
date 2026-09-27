package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.border.LineBorder;

import lombok.Getter;
import lu.kbra.modelizer_next.domain.FieldTags;

@Getter
public class FieldTagsPopupMenu extends LivePopupMenu {

	private static final long serialVersionUID = 2155488607785939774L;

	private final Consumer<FieldTags> confirm;

	private final JCheckBoxMenuItem primaryKey;
	private final JCheckBoxMenuItem unique;
	private final JCheckBoxMenuItem nonNull;
	private final JCheckBoxMenuItem autoIncrement;
	private final JCheckBoxMenuItem generated;
	private final JMenuItem actionItem;

	public FieldTagsPopupMenu(final Consumer<FieldTags> confirm) {
		super.setBorder(new LineBorder(Color.BLACK, 1));
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.confirm = confirm;

		this.primaryKey = new JCheckBoxMenuItem("Primary key", false);
		this.unique = new JCheckBoxMenuItem("Unique", false);
		this.nonNull = new JCheckBoxMenuItem("Non null", true);
		this.autoIncrement = new JCheckBoxMenuItem("Auto Increment", false);
		this.generated = new JCheckBoxMenuItem("Generated", false);

		this.actionItem = new JMenuItem("Apply");
		this.actionItem.addActionListener(this::invokeConfirm);

		super.add(this.primaryKey);
		super.add(this.unique);
		super.add(this.nonNull);
		super.add(this.autoIncrement);
		super.add(this.generated);
		super.add(this.actionItem);
	}

	public void apply(final FieldTags data) {
		this.primaryKey.setSelected(data.isPrimaryKey());
		this.unique.setSelected(data.isUnique());
		this.nonNull.setSelected(data.isNonNull());
		this.autoIncrement.setSelected(data.isAutoIncrement());
		this.generated.setSelected(data.isGenerated());
	}

	public boolean isPrimaryKey() {
		return this.primaryKey.isSelected();
	}

	public boolean isUnique() {
		return this.unique.isSelected();
	}

	public boolean isNonNull() {
		return this.nonNull.isSelected();
	}

	public boolean isAutoIncrement() {
		return this.autoIncrement.isSelected();
	}

	public boolean isGenerated() {
		return this.generated.isSelected();
	}

	@Override
	public void invokeConfirm(final ActionEvent e) {
		this.confirm
				.accept(new FieldTags(this.isPrimaryKey(), this.isUnique(), this.isNonNull(), this.isAutoIncrement(), this.isGenerated()));
	}

}
