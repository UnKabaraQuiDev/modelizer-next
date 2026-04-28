package lu.kbra.modelizer_next.ui.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

final class DiagramCanvasActionRegistrar {

	record DiagramCanvasActions(Runnable renameSelection, IntConsumer moveFieldSelection, IntConsumer moveSelectedFieldInList,
			Runnable addTable, Runnable addField, Runnable addComment, Runnable deleteSelection, Runnable duplicateSelection,
			Runnable clearSelection, Runnable addLink, Runnable selectAll, Runnable edit, Runnable copySelection, Runnable cutSelection,
			Runnable pasteSelection, Runnable undo, Runnable redo) {
	}

	private DiagramCanvasActionRegistrar() {
	}

	static void installDefault(final JComponent component, final DiagramCanvasActions actions) {
		final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = component.getActionMap();

		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo", actions.undo());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				"redo",
				actions.redo());

		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSelection", actions.renameSelection());
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
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK),
				"addComment",
				actions.addComment());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit", actions.edit());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelection", actions.deleteSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK),
				"deleteSelection",
				actions.deleteSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
				"duplicateSelection",
				actions.duplicateSelection());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection", actions.clearSelection());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "addLink", actions.addLink());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
				"selectAll",
				actions.selectAll());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
				"copySelection",
				actions.copySelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK),
				"cutSelection",
				actions.cutSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
				"pasteSelection",
				actions.pasteSelection());
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
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
	}
}
