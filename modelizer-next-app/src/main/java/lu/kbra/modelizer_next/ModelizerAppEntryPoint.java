package lu.kbra.modelizer_next;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;

import lu.kbra.modelizer_next.bootstrap.AppMain;
import lu.kbra.modelizer_next.common.SampleDocumentFactory;
import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.ui.MainFrame;

public class ModelizerAppEntryPoint implements AppMain {

	@Override
	public void start() {
		try {
			App.init();
			System.out.println(App.NAME + " / " + App.VERSION + " (" + App.REVISION + ")");
			System.out.println("App dir: " + App.getAppDirectory());
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Uh uh ! It seems like this app's manifest is malformed, try\nrestarting it, redownloading it or updating it.\nIf nothing works, please report to: "
							+ App.ISSUES_URL,
					"Manifest error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			MNMain.applyConfiguredLookAndFeel();
			final ModelDocument document = SampleDocumentFactory.create();
			final MainFrame frame = new MainFrame(document);
			frame.setTitle(App.title(document.getMeta().getName()));
			frame.setVisible(true);
		});
	}

}
