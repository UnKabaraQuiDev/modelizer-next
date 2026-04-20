package lu.kbra.modelizer_next.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

final class DiagramCanvasActionRegistrar {

	private DiagramCanvasActionRegistrar() {
	}

	static void install(final JComponent component, final DiagramCanvasActions actions) {
		final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = component.getActionMap();

		DiagramCanvasActionRegistrar.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSelection",
				actions.renameSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				"selectFieldUp",
				() -> actions.moveFieldSelection().accept(-1));
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				"selectFieldDown",
				() -> actions.moveFieldSelection().accept(1));
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
				"moveFieldUp",
				() -> actions.moveSelectedFieldInList().accept(-1));
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
				"moveFieldDown",
				() -> actions.moveSelectedFieldInList().accept(1));
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK),
				"addTable",
				actions.addTable());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
				"addField",
				actions.addField());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
				"addComment",
				actions.addComment());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"deleteSelection",
				actions.deleteSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
				"duplicateSelection",
				actions.duplicateSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				"clearSelection",
				actions.clearSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
				"addLink",
				actions.addLink());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
				"selectAll",
				actions.selectAll());
	}

	private static void bind(
			final InputMap inputMap,
			final ActionMap actionMap,
			final KeyStroke keyStroke,
			final String actionKey,
			final Runnable action) {
		inputMap.put(keyStroke, actionKey);
		actionMap.put(actionKey, new AbstractAction() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				action.run();
			}
		});
	}

	record DiagramCanvasActions(
			Runnable renameSelection,
			IntConsumer moveFieldSelection,
			IntConsumer moveSelectedFieldInList,
			Runnable addTable,
			Runnable addField,
			Runnable addComment,
			Runnable deleteSelection,
			Runnable duplicateSelection,
			Runnable clearSelection,
			Runnable addLink,
			Runnable selectAll) {
	}
}
