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

import lu.kbra.modelizer_next.ui.frame.MainFrame;

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
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER), "undo", actions.undo());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, MainFrame.CTRL_MODIFIER | InputEvent.SHIFT_DOWN_MASK),
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
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_T, MainFrame.CTRL_MODIFIER), "addTable", actions.addTable());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_F, MainFrame.CTRL_MODIFIER), "addField", actions.addField());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK),
				"addComment",
				actions.addComment());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, MainFrame.CTRL_MODIFIER), "edit", actions.edit());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelection", actions.deleteSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK),
				"deleteSelection",
				actions.deleteSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, MainFrame.CTRL_MODIFIER),
				"duplicateSelection",
				actions.duplicateSelection());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection", actions.clearSelection());
		DiagramCanvasActionRegistrar
				.bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_L, MainFrame.CTRL_MODIFIER), "addLink", actions.addLink());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, MainFrame.CTRL_MODIFIER),
				"selectAll",
				actions.selectAll());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, MainFrame.CTRL_MODIFIER),
				"copySelection",
				actions.copySelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, MainFrame.CTRL_MODIFIER),
				"cutSelection",
				actions.cutSelection());
		DiagramCanvasActionRegistrar.bind(inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, MainFrame.CTRL_MODIFIER),
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
