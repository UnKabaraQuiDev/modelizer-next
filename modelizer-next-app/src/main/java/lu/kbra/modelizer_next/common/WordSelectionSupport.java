package lu.kbra.modelizer_next.common;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import lu.kbra.modelizer_next.ui.frame.MainFrame;

public final class WordSelectionSupport {

	private static final String DELIMITERS = "-_ ";

	public static <T extends JTextComponent> T install(final T field) {
		final InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap am = field.getActionMap();

		// Select by word
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK), "select-next-word-custom");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK), "select-prev-word-custom");

		am.put("select-next-word-custom", new SelectNextWordAction(field));
		am.put("select-prev-word-custom", new SelectPreviousWordAction(field));

		// Move by word
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, MainFrame.CTRL_MODIFIER), "next-word-custom");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, MainFrame.CTRL_MODIFIER), "prev-word-custom");

		am.put("next-word-custom", new MoveNextWordAction(field));
		am.put("prev-word-custom", new MovePreviousWordAction(field));

		return field;
	}

	private static boolean isDelimiter(final char c) {
		return WordSelectionSupport.DELIMITERS.indexOf(c) >= 0;
	}

	private static int nextWord(final String text, int pos) {
		final int n = text.length();

		// Skip current word
		while (pos < n && !WordSelectionSupport.isDelimiter(text.charAt(pos))) {
			pos++;
		}

		// Skip delimiters
		while (pos < n && WordSelectionSupport.isDelimiter(text.charAt(pos))) {
			pos++;
		}

		return pos;
	}

	private static int previousWord(final String text, int pos) {
		if (pos == 0) {
			return 0;
		}

		pos--;

		// Skip delimiters to the left
		while (pos > 0 && WordSelectionSupport.isDelimiter(text.charAt(pos))) {
			pos--;
		}

		// Skip previous word
		while (pos > 0 && !WordSelectionSupport.isDelimiter(text.charAt(pos - 1))) {
			pos--;
		}

		return pos;
	}

	private static class SelectNextWordAction extends AbstractAction {

		private static final long serialVersionUID = -6878892377807541429L;
		private final JTextComponent field;

		SelectNextWordAction(final JTextComponent field) {
			this.field = field;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int dot = this.field.getCaretPosition();
			final int mark = this.field.getCaret().getMark();

			final int next = WordSelectionSupport.nextWord(this.field.getText(), dot);
			this.field.getCaret().setDot(mark);
			this.field.getCaret().moveDot(next);
		}

	}

	private static class SelectPreviousWordAction extends AbstractAction {

		private static final long serialVersionUID = -1495881918407046092L;
		private final JTextComponent field;

		SelectPreviousWordAction(final JTextComponent field) {
			this.field = field;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int dot = this.field.getCaretPosition();
			final int mark = this.field.getCaret().getMark();

			final int prev = WordSelectionSupport.previousWord(this.field.getText(), dot);
			this.field.getCaret().setDot(mark);
			this.field.getCaret().moveDot(prev);
		}

	}

	private static class MoveNextWordAction extends AbstractAction {

		private static final long serialVersionUID = 8523725209368747993L;
		private final JTextComponent field;

		MoveNextWordAction(final JTextComponent field) {
			this.field = field;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int next = WordSelectionSupport.nextWord(this.field.getText(), this.field.getCaretPosition());
			this.field.getCaret().setDot(next);
		}
	}

	private static class MovePreviousWordAction extends AbstractAction {

		private static final long serialVersionUID = -2952414927303770186L;
		private final JTextComponent field;

		MovePreviousWordAction(final JTextComponent field) {
			this.field = field;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int prev = WordSelectionSupport.previousWord(this.field.getText(), this.field.getCaretPosition());
			this.field.getCaret().setDot(prev);
		}
	}

}
