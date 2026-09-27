package lu.kbra.modelizer_next.cmdline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;

import lu.kbra.model_exporter.api.ExportUpdateCallback;
import lu.kbra.pclib.PCUtils;

public final class InteractiveProgressRenderer implements ProgressRenderer {

	private final Terminal terminal;
	private final Display display;

	private Row root;

	private final Map<ExportUpdateCallback, Row> lookup = new IdentityHashMap<>();

	private int renderedLines;

	public InteractiveProgressRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().system(true).devTty(true).build();

			this.display = new Display(this.terminal, false);

		} catch (final Exception e) {
			throw new RuntimeException("Could not initialize terminal", e);
		}
	}

	@Override
	public void sectionCreated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		this.ensureRow(parent, section);
		this.render();
	}

	@Override
	public void progressUpdated(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		this.ensureRow(parent, section);
		this.render();
	}

	@Override
	public void sectionDeleted(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		final Row row = this.lookup.get(section);

		if (row != null) {

			this.removeChildren(row);

			row.finished = true;
		}

		this.render();
	}

	private Row ensureRow(final ExportUpdateCallback parent, final ExportUpdateCallback section) {
		final ExportUpdateCallback actualParent = parent != null ? parent
				: section == null ? null
				: section.getParent();

		if (actualParent != null) {
			this.ensureRoot(actualParent);
		}

		if (section == null) {
			return this.lookup.get(actualParent);
		}

		final Row existing = this.lookup.get(section);

		if (existing != null) {
			return existing;
		}

		final Row parentRow = actualParent == null ? this.root : this.ensureParentRow(actualParent);

		final Row row = new Row(section, parentRow);

		this.lookup.put(section, row);

		if (parentRow != null) {
			parentRow.children.add(row);
		}

		return row;
	}

	private Row ensureRoot(final ExportUpdateCallback actualParent) {

		if (actualParent == null) {
			return null;
		}

		if (this.root != null) {
			return this.root;
		}

		final ExportUpdateCallback rootSection = this.findRootSection(actualParent);

		final Row existing = this.lookup.get(rootSection);

		if (existing != null) {
			this.root = existing;
			return existing;
		}

		this.root = new Row(rootSection, null);

		this.lookup.put(rootSection, this.root);

		return this.root;
	}

	private ExportUpdateCallback findRootSection(final ExportUpdateCallback section) {

		ExportUpdateCallback current = section;

		while (current.getParent() != null) {
			current = current.getParent();
		}

		return current;
	}

	private Row ensureParentRow(final ExportUpdateCallback section) {
		if ((section == null) || (section == this.root.section)) {
			return this.root;
		}

		final Row existing = this.lookup.get(section);

		if (existing != null) {
			return existing;
		}

		final ExportUpdateCallback parent = section.getParent();

		final Row parentRow = parent == null ? this.root : this.ensureParentRow(parent);

		final Row row = new Row(section, parentRow);

		this.lookup.put(section, row);

		if (parentRow != null) {
			parentRow.children.add(row);
		}

		return row;
	}

	private void removeChildren(final Row row) {
		for (final Row child : row.children) {
			this.removeChildren(child);

			this.lookup.remove(child.section);
		}

		row.children.clear();
	}

	private void render() {
		final List<AttributedString> lines = new ArrayList<>();

		if (this.root != null) {
			this.renderRow(this.root, lines);
		}

		final int displayLines = Math.max(lines.size(), this.renderedLines);

		while (lines.size() < displayLines) {
			lines.add(new AttributedString(""));
		}

		final int rows = Math.max(1, this.terminal.getHeight());

		final int columns = Math.max(1, this.terminal.getWidth());

		this.display.resize(rows, columns);

		this.display.update(lines, -1);

		this.renderedLines = displayLines;
	}

	private void renderRow(final Row row, final List<AttributedString> lines) {

		lines.add(AttributedString.fromAnsi(this.format(row)));

		for (final Row child : row.children) {
			this.renderRow(child, lines);
		}
	}

	private String format(final Row row) {
		final ExportUpdateCallback section = row.section;

		final String name = this.getName(section);

		final int depth = this.getTreeDepth(row);

		final String prefix = depth == 0 ? "" : "| ".repeat(depth);

		if (row.finished) {

			final String endMessage = section.getEndMessage();

			return prefix + name + " " + (endMessage == null || endMessage.isEmpty() ? "done" : endMessage);
		}

		final double progress = this.getProgress(section);

		final int terminalWidth = Math.max(1, this.terminal.getWidth());

		final int fixedWidth = prefix.length() + name.length() + 12;

		final int barWidth = Math.max(5, terminalWidth - fixedWidth);

		return prefix + name + " " + this.progressBar(progress, barWidth) + " "
				+ PCUtils.leftPadString(Double.toString(PCUtils.round(progress, 1)), " ", 5) + " %";
	}

	private int getTreeDepth(final Row row) {

		int depth = 0;

		Row current = row.parent;

		while (current != null) {
			depth++;
			current = current.parent;
		}

		return depth;
	}

	private String progressBar(double progress, final int width) {
		progress = Math.max(0.0, Math.min(100.0, progress));

		final int filled = (int) Math.round(progress / 100.0 * width);

		return "[" + "=".repeat(filled) + " ".repeat(width - filled) + "]";
	}

	private String getName(final ExportUpdateCallback section) {
		final String name = section.getName();

		return name == null ? "" : name;
	}

	private double getProgress(final ExportUpdateCallback section) {
		return section.getProgress();
	}

	@Override
	public void close() {
		try {
			this.terminal.close();
		} catch (final IOException e) {
			// Ignore.
		}
	}

	private static final class Row {

		final ExportUpdateCallback section;
		final Row parent;

		final List<Row> children = new ArrayList<>();

		boolean finished;

		Row(final ExportUpdateCallback section, final Row parent) {

			this.section = section;
			this.parent = parent;
		}
	}

}
