package lu.kbra.code_exporter.java.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JavaTypesUiPanel extends JPanel {

	private static final long serialVersionUID = -8860644393735712583L;

	private final JTable table;
	private final MappingTableModel model;

	private List<MappingData> data = new ArrayList<>();

	public JavaTypesUiPanel() {
		super(new BorderLayout(5, 5));

		final String[] columns = { "Regex", "Class", "Annotations" };

		this.model = new MappingTableModel(columns, this.data);

		this.table = new JTable(this.model);

		this.table.setRowHeight(80);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		this.table.getColumnModel().getColumn(0).setPreferredWidth(250);
		this.table.getColumnModel().getColumn(1).setPreferredWidth(220);
		this.table.getColumnModel().getColumn(2).setPreferredWidth(500);

		this.table.getColumnModel().getColumn(2).setCellRenderer(new AnnotationTableRenderer());
		this.table.getColumnModel().getColumn(2).setCellEditor(new AnnotationTableEditor());

		this.table.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));

		this.add(new JScrollPane(this.table), BorderLayout.CENTER);

		// Main table buttons
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JButton addButton = new JButton("+");
		final JButton removeButton = new JButton("-");

		addButton.setToolTipText("Add mapping");
		removeButton.setToolTipText("Remove mapping");

		addButton.addActionListener(e -> this.addMapping());
		removeButton.addActionListener(e -> this.removeMapping());

		buttons.add(addButton);
		buttons.add(removeButton);

		this.add(buttons, BorderLayout.SOUTH);
	}

	public void rebuild(final List<MappingData> newData) {
		// Stop any active cell editor first.
		if (this.table.isEditing()) {
			this.table.getCellEditor().stopCellEditing();
		}

		// Replace the contents of the existing list.
		this.data.clear();
		this.data.addAll(newData);

		// Tell the JTable that the complete model has changed.
		this.model.fireTableDataChanged();

		// Restore the preferred column widths.
		this.table.getColumnModel().getColumn(0).setPreferredWidth(250);
		this.table.getColumnModel().getColumn(1).setPreferredWidth(220);
		this.table.getColumnModel().getColumn(2).setPreferredWidth(500);

		// Make sure the table is laid out again.
		this.table.revalidate();
		this.table.repaint();
		this.revalidate();
		this.repaint();
	}

	private void addMapping() {
		this.model.addMapping(new MappingData("", "java.lang.String", new ArrayList<>()));

		final int row = this.model.getRowCount() - 1;

		this.table.setRowSelectionInterval(row, row);
		this.table.editCellAt(row, 0);

		this.table.requestFocusInWindow();
	}

	private void removeMapping() {
		final int row = this.table.getSelectedRow();

		if (row < 0) {
			return;
		}

		this.model.removeMapping(row);
	}

	// ------------------------------------------------------------
	// Data
	// ------------------------------------------------------------

	public record AnnotationData(String className, String parameters) {
		public AnnotationData(final Class<? extends Annotation> className, final String parameters) {
			this(className.getName(), parameters);
		}
	}

	public record MappingData(String regex, String className, List<AnnotationData> annotations) {
		public MappingData(final String regex, final Class<?> className, final List<AnnotationData> annotations) {
			this(regex, className.getName(), annotations);
		}
	}

	// ------------------------------------------------------------
	// Main table model
	// ------------------------------------------------------------

	static class MappingTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 3601095704877587899L;
		private final String[] columns;
		private final List<MappingData> data;

		MappingTableModel(final String[] columns, final List<MappingData> data) {
			this.columns = columns;
			this.data = data;
		}

		@Override
		public int getRowCount() {
			return this.data.size();
		}

		@Override
		public int getColumnCount() {
			return this.columns.length;
		}

		@Override
		public String getColumnName(final int column) {
			return this.columns[column];
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			final MappingData mapping = this.data.get(row);

			return switch (column) {
			case 0 -> mapping.regex();
			case 1 -> mapping.className();
			case 2 -> mapping.annotations();
			default -> null;
			};
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return true;
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column) {
			final MappingData old = this.data.get(row);

			final MappingData updated = switch (column) {
			case 0 -> new MappingData(String.valueOf(value), old.className(), old.annotations());

			case 1 -> new MappingData(old.regex(), String.valueOf(value), old.annotations());

			case 2 -> new MappingData(old.regex(), old.className(), new ArrayList<>((List<AnnotationData>) value));

			default -> old;
			};

			this.data.set(row, updated);

			this.fireTableCellUpdated(row, column);
		}

		void addMapping(final MappingData mapping) {
			final int row = this.data.size();

			this.data.add(mapping);

			this.fireTableRowsInserted(row, row);
		}

		void removeMapping(final int row) {
			if (row < 0 || row >= this.data.size()) {
				return;
			}

			this.data.remove(row);

			this.fireTableRowsDeleted(row, row);
		}

		List<MappingData> getMappings() {
			return List.copyOf(this.data);
		}
	}

	// ------------------------------------------------------------
	// Annotation renderer
	// ------------------------------------------------------------

	static class AnnotationTableRenderer implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(
				final JTable parent,
				final Object value,
				final boolean isSelected,
				final boolean hasFocus,
				final int row,
				final int column) {
			final List<AnnotationData> annotations = (List<AnnotationData>) value;

			final JPanel panel = new JPanel(new BorderLayout());

			final DefaultTableModel model = new DefaultTableModel(new String[] { "Annotation class", "Parameters" }, 0) {
				@Override
				public boolean isCellEditable(final int row, final int column) {
					return false;
				}
			};

			for (final AnnotationData annotation : annotations) {
				model.addRow(new Object[] { annotation.className(), annotation.parameters() });
			}

			final JTable annotationTable = new JTable(model);

			annotationTable.setRowHeight(22);
			annotationTable.setShowGrid(true);

			annotationTable.getColumnModel().getColumn(0).setPreferredWidth(350);
			annotationTable.getColumnModel().getColumn(1).setPreferredWidth(150);

			panel.add(annotationTable, BorderLayout.CENTER);

			return panel;
		}
	}

	// ------------------------------------------------------------
	// Annotation editor
	// ------------------------------------------------------------

	static class AnnotationTableEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 765874229679536251L;

		private final JPanel panel = new JPanel(new BorderLayout(3, 3));

		private final JTable table = new JTable();

		private final DefaultTableModel model;

		AnnotationTableEditor() {

			this.model = new DefaultTableModel(new String[] { "Annotation class", "Parameters" }, 0);

			this.table.setModel(this.model);
			this.table.setRowHeight(22);

			this.table.getColumnModel().getColumn(0).setPreferredWidth(350);
			this.table.getColumnModel().getColumn(1).setPreferredWidth(150);

			this.panel.add(table, BorderLayout.CENTER);

			final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

			final JButton addButton = new JButton("+");
			final JButton removeButton = new JButton("-");

			addButton.setToolTipText("Add annotation");

			removeButton.setToolTipText("Remove annotation");

			addButton.addActionListener(e -> this.addAnnotation());

			removeButton.addActionListener(e -> this.removeAnnotation());

			buttons.add(addButton);
			buttons.add(removeButton);

			this.panel.add(buttons, BorderLayout.SOUTH);
		}

		private void addAnnotation() {
			final int row = this.model.getRowCount();

			this.model.addRow(new Object[] { "", "" });

			this.table.setRowSelectionInterval(row, row);
			this.table.editCellAt(row, 0);
			this.table.requestFocusInWindow();

			this.updateEditorHeight();
		}

		private void removeAnnotation() {
			final int row = this.table.getSelectedRow();

			if (row < 0) {
				return;
			}

			this.model.removeRow(row);

			this.updateEditorHeight();
		}

		private void updateEditorHeight() {
			final int rowCount = Math.max(1, this.table.getRowCount());

			final int height = rowCount * this.table.getRowHeight() + 45;

			this.panel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, height));

			this.panel.revalidate();
		}

		@Override
		public Component getTableCellEditorComponent(
				final JTable parent,
				final Object value,
				final boolean isSelected,
				final int row,
				final int column) {
			this.model.setRowCount(0);

			final List<AnnotationData> annotations = (List<AnnotationData>) value;

			for (final AnnotationData annotation : annotations) {
				this.model.addRow(new Object[] { annotation.className(), annotation.parameters() });
			}

			this.updateEditorHeight();

			return this.panel;
		}

		@Override
		public Object getCellEditorValue() {
			final List<AnnotationData> result = new ArrayList<>();

			for (int i = 0; i < this.model.getRowCount(); i++) {
				result.add(new AnnotationData(String.valueOf(this.model.getValueAt(i, 0)), String.valueOf(this.model.getValueAt(i, 1))));
			}

			return result;
		}
	}

}
