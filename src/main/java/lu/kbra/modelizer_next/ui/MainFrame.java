package lu.kbra.modelizer_next.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.layout.PanelType;

public class MainFrame extends JFrame {

	private final ModelDocument document;
	private final JLabel statusLabel;
	private final JLabel selectionPathLabel;
	private final JTabbedPane tabbedPane;
	private final DiagramCanvas conceptualCanvas;
	private final DiagramCanvas logicalCanvas;
	private final DiagramCanvas physicalCanvas;

	public MainFrame(final ModelDocument document) {
		super("Modelizer Next");
		this.document = document;

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.statusLabel = new JLabel(
				"Left drag: move object   |   Middle drag: pan   |   Mouse wheel: zoom   |   Right drag from field to field: create link",
				SwingConstants.LEFT);
		this.selectionPathLabel = new JLabel("No selection", SwingConstants.RIGHT);

		final CanvasStatusListener statusListener = selectionInfo -> {
			if (getActiveCanvas() != null && getActiveCanvas().getPanelType() == selectionInfo.panelType()) {
				updateSelectionLabel(selectionInfo);
			}
		};

		this.conceptualCanvas = new DiagramCanvas(this.document, PanelType.CONCEPTUAL, statusListener);
		this.logicalCanvas = new DiagramCanvas(this.document, PanelType.LOGICAL, statusListener);
		this.physicalCanvas = new DiagramCanvas(this.document, PanelType.PHYSICAL, statusListener);

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.addTab("Conceptual", this.conceptualCanvas);
		this.tabbedPane.addTab("Logical", this.logicalCanvas);
		this.tabbedPane.addTab("Physical", this.physicalCanvas);
		this.tabbedPane.addChangeListener(event -> updateSelectionLabel(getActiveCanvas().getSelectionInfo()));

		final JPanel statusPanel = new JPanel(new BorderLayout(12, 0));
		statusPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
		statusPanel.add(this.statusLabel, BorderLayout.WEST);
		statusPanel.add(this.selectionPathLabel, BorderLayout.EAST);

		this.add(this.tabbedPane, BorderLayout.CENTER);
		this.add(statusPanel, BorderLayout.SOUTH);
		this.setSize(1200, 800);
		this.setLocationRelativeTo(null);

		updateSelectionLabel(getActiveCanvas().getSelectionInfo());
	}

	private DiagramCanvas getActiveCanvas() {
		return switch (this.tabbedPane.getSelectedIndex()) {
		case 0 -> this.conceptualCanvas;
		case 1 -> this.logicalCanvas;
		case 2 -> this.physicalCanvas;
		default -> this.conceptualCanvas;
		};
	}

	private void updateSelectionLabel(final SelectionInfo selectionInfo) {
		final String path = selectionInfo == null || selectionInfo.path() == null || selectionInfo.path().isBlank()
				? "No selection"
				: selectionInfo.path();
		this.selectionPathLabel.setText(path);
	}

}