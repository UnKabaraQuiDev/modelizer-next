package lu.kbra.modelizer_next;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

	protected UMLFile file = new UMLFile();

	protected final JTabbedPane tabbedPane;
	protected final ConceptualPanel conceptualPanel;

	public MainFrame() {
		super(Consts.NAME + " - " + Consts.VERSION);

		super.setLayout(new BorderLayout());
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Conceptual", conceptualPanel = new ConceptualPanel(file));
		super.add(tabbedPane, BorderLayout.CENTER);

		createClass();
		
		setSize(500, 400);
	}

	public UMLClass createClass() {
		final UMLClass c = new UMLClass();
		c.setConceptualName("Table_" + file.classes.size());
		c.setPosition(new Point2D.Float());
		file.classes.add(c);
		conceptualPanel.updateModel();
		return c;
	}

}
