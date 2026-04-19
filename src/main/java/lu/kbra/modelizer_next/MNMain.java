package lu.kbra.modelizer_next;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.ui.MainFrame;
import lu.kbra.modelizer_next.ui.SampleDocumentFactory;

public class MNMain {

	public static final ObjectMapper OBJECT_MAPPER = createMapper();

	public static void main(String[] args) {
		try {
			App.init();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Uh uh ! It seems like this app is malformed, try restarting it, redownloading it or updating it. If nothing works, please report to: "
							+ App.ISSUES_URL,
					"Hmmmmmm", JOptionPane.ERROR_MESSAGE);
		}

		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (final Exception ignored) {
				// keep default look and feel
			}

			final ModelDocument document = SampleDocumentFactory.create();
			final MainFrame frame = new MainFrame(document);
			frame.setTitle(App.title( document.getMeta().getName()));
			frame.setVisible(true);
		});
	}

	private static ObjectMapper createMapper() {
		final ObjectMapper mapper = new ObjectMapper(
				JsonFactory.builder().configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
						.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true).build());

		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		return mapper;
	}

}
