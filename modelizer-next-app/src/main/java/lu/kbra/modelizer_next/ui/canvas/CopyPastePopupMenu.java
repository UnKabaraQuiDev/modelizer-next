package lu.kbra.modelizer_next.ui.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import lu.kbra.modelizer_next.ui.canvas.datastruct.CopyPasteSpecialData;

public class CopyPastePopupMenu extends LivePopupMenu {

	private static final long serialVersionUID = 8880719050925219141L;

	private final Consumer<CopyPasteSpecialData> confirm;

	private final JCheckBoxMenuItem keepOutgoingLinks;
	private final JCheckBoxMenuItem keepInternalLinks;
	private final JCheckBoxMenuItem keepLinks;
	private final JCheckBoxMenuItem withDefaultStyle;
	private final JLabel nameLabel;
	private final JTextField nameField;
	private final JMenuItem actionItem;

	public CopyPastePopupMenu(final Consumer<CopyPasteSpecialData> confirm) {
		super.setBorder(new LineBorder(Color.BLACK, 1));
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.confirm = confirm;

		this.keepOutgoingLinks = new JCheckBoxMenuItem("Keep outgoing links", true);
		this.keepInternalLinks = new JCheckBoxMenuItem("Keep internal links", true);
		this.keepLinks = new JCheckBoxMenuItem("Keep links", true);
		this.withDefaultStyle = new JCheckBoxMenuItem("With default style", false);

		final JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.add(this.nameLabel = new JLabel("Edit name:  "), BorderLayout.WEST);
		namePanel.add(this.nameField = new JTextField("%% Copy", 15), BorderLayout.CENTER);

		this.actionItem = new JMenuItem("Paste");
		this.actionItem.addActionListener(this::invokeConfirm);

		super.add(this.keepOutgoingLinks);
		super.add(this.keepInternalLinks);
		super.add(this.keepLinks);
		super.add(this.withDefaultStyle);
		super.add(namePanel);
		super.add(this.actionItem);

	}

	public JCheckBoxMenuItem getKeepOutgoingLinks() {
		return this.keepOutgoingLinks;
	}

	public JCheckBoxMenuItem getKeepInternalLinks() {
		return this.keepInternalLinks;
	}

	public JCheckBoxMenuItem getKeepLinks() {
		return this.keepLinks;
	}

	public JCheckBoxMenuItem getWithDefaultStyle() {
		return this.withDefaultStyle;
	}

	public JLabel getNameLabel() {
		return this.nameLabel;
	}

	public JTextField getNameField() {
		return this.nameField;
	}

	public JMenuItem getActionItem() {
		return this.actionItem;
	}

	public boolean isKeepOutgoingLinks() {
		return this.keepOutgoingLinks.isSelected();
	}

	public boolean isKeepInternalLinks() {
		return this.keepInternalLinks.isSelected();
	}

	public boolean isKeepLinks() {
		return this.keepLinks.isSelected();
	}

	public boolean isWithDefaultStyle() {
		return this.withDefaultStyle.isSelected();
	}

	public String getNameOverwrite() {
		return this.nameField.getText();
	}

	public void setActionLabel(final String l) {
		this.actionItem.setText(l);
	}

	@Override
	public void invokeConfirm(final ActionEvent e) {
		this.confirm.accept(new CopyPasteSpecialData(this.isKeepOutgoingLinks(),
				this.isKeepInternalLinks(),
				this.isKeepLinks(),
				this.isWithDefaultStyle(),
				this.getNameOverwrite()));
	}

}
