package lu.kbra.modelizer_next;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class UMLFile {

	protected List<UMLClass> classes;
	protected List<UMLConceptualLink> conceptualLinks;
	protected List<UMLLogicalLink> logicalLinks;

	public UMLFile() {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	public UMLFile(List<UMLClass> classes, List<UMLConceptualLink> conceptualLinks, List<UMLLogicalLink> logicalLinks) {
		this.classes = classes;
		this.conceptualLinks = conceptualLinks;
		this.logicalLinks = logicalLinks;
	}

	public UMLClass createClass() {
		final UMLClass c = new UMLClass();
		c.setConceptualName("Table_" + classes.size());
		c.setPosition(new Point2D.Float());
		classes.add(c);
		return c;
	}

	public List<UMLClass> getClasses() {
		return classes;
	}

	public List<UMLConceptualLink> getConceptualLinks() {
		return conceptualLinks;
	}

	public List<UMLLogicalLink> getLogicalLinks() {
		return logicalLinks;
	}

	@Override
	public String toString() {
		return "UMLFile@" + System.identityHashCode(this) + " [classes=" + classes + ", conceptualLinks=" + conceptualLinks
				+ ", logicalLinks=" + logicalLinks + "]";
	}

}
