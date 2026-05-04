package lu.kbra.modelizer_next.common;

import java.awt.geom.Point2D;

import lu.kbra.modelizer_next.document.ModelDocument;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;

public final class SampleDocumentFactory {

	public static final String META_NAME = "Demo model";

	public static ModelDocument create() {
		final ModelDocument document = new ModelDocument();
		document.getMeta().setName(SampleDocumentFactory.META_NAME);

		final ClassModel customer = new ClassModel();
		customer.setConceptualName("Customer");
		customer.setTechnicalName("T_CUSTOMER");
//		customer.setComment("Stores the master customer record.");
		customer.getFields().add(SampleDocumentFactory.field("Customer ID", "CUSTOMER_ID", false, true, true, true));
		customer.getFields().add(SampleDocumentFactory.field("Display name", "DISPLAY_NAME", false, false, false, true));
		customer.getFields().add(SampleDocumentFactory.field("Email", "EMAIL", false, false, true, false));

		final ClassModel order = new ClassModel();
		order.setConceptualName("Order");
		order.setTechnicalName("T_ORDER");
//		order.setComment("A placed order.");
		order.getFields().add(SampleDocumentFactory.field("Order ID", "ORDER_ID", false, true, true, true));
		order.getFields().add(SampleDocumentFactory.field("Customer ID", "CUSTOMER_ID", true, false, false, true));
		order.getFields().add(SampleDocumentFactory.field("Created at", "CREATED_AT", true, false, false, true));

		final LinkModel conceptualCustomerOrders = new LinkModel();
		conceptualCustomerOrders.setName("places");
		conceptualCustomerOrders.setFrom(new LinkEnd(customer.getId(), null));
		conceptualCustomerOrders.setTo(new LinkEnd(order.getId(), null));
		conceptualCustomerOrders.setCardinalityFrom(Cardinality.ONE);
		conceptualCustomerOrders.setCardinalityTo(Cardinality.ZERO_OR_MANY);
//		conceptualCustomerOrders.setComment("One customer can place many orders.");

		final LinkModel logicalCustomerOrders = new LinkModel();
		logicalCustomerOrders.setName("FK_ORDER_CUSTOMER");
		logicalCustomerOrders.setFrom(new LinkEnd(order.getId(), order.getFields().get(1).getId()));
		logicalCustomerOrders.setTo(new LinkEnd(customer.getId(), customer.getFields().get(0).getId()));
		logicalCustomerOrders.setCardinalityFrom(null);
		logicalCustomerOrders.setCardinalityTo(null);
//		logicalCustomerOrders.setComment("Order.CUSTOMER_ID references Customer.CUSTOMER_ID.");

		final CommentModel note = new CommentModel();
		note.setKind(CommentKind.STANDALONE);
		note.setText("Standalone notes can live anywhere on the canvas.");

		final CommentModel customerComment = new CommentModel("Comments can be bound to a class.");
		customerComment.setKind(CommentKind.BOUND);
		customerComment.setBinding(new CommentBinding(BoundTargetType.CLASS, customer.getId()));

		final CommentModel conceptualLinkComment = new CommentModel("Comments can be bound to a conceptual link.");
		conceptualLinkComment.setKind(CommentKind.BOUND);
		conceptualLinkComment.setBinding(new CommentBinding(BoundTargetType.LINK, conceptualCustomerOrders.getId()));

		final CommentModel technicalLinkComment = new CommentModel("Comments can be bound to a technical link.");
		technicalLinkComment.setKind(CommentKind.BOUND);
		technicalLinkComment.setBinding(new CommentBinding(BoundTargetType.LINK, logicalCustomerOrders.getId()));

		document.getModel().getClasses().add(customer);
		document.getModel().getClasses().add(order);
		document.getModel().getConceptualLinks().add(conceptualCustomerOrders);
		document.getModel().getTechnicalLinks().add(logicalCustomerOrders);
		document.getModel().getComments().add(note);
		document.getModel().getComments().add(customerComment);
		document.getModel().getComments().add(conceptualLinkComment);
		document.getModel().getComments().add(technicalLinkComment);

		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL), customer, 120, 120, 220, 0);
		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL), order, 450, 170, 220, 0);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL), note, 120, 320, 280, 90);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL), customerComment, 410, 70, 250, 80);
		SampleDocumentFactory
				.seedComment(document.getWorkspace().getPanels().get(PanelType.CONCEPTUAL), conceptualLinkComment, 300, 210, 260, 80);

		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.LOGICAL), customer, 120, 120, 220, 0);
		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.LOGICAL), order, 450, 170, 220, 0);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.LOGICAL), note, 120, 340, 280, 90);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.LOGICAL), customerComment, 410, 70, 250, 80);

		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.PHYSICAL), customer, 120, 120, 220, 0);
		SampleDocumentFactory.seedClass(document.getWorkspace().getPanels().get(PanelType.PHYSICAL), order, 450, 170, 220, 0);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.PHYSICAL), note, 120, 340, 280, 90);
		SampleDocumentFactory.seedComment(document.getWorkspace().getPanels().get(PanelType.PHYSICAL), customerComment, 410, 70, 250, 80);

		return document;
	}

	private static FieldModel field(
			final String conceptualName,
			final String technicalName,
			final boolean notConceptual,
			final boolean primaryKey,
			final boolean unique,
			final boolean notNull) {
		final FieldModel field = new FieldModel();
		field.setConceptualName(conceptualName);
		field.setTechnicalName(technicalName);
		field.setNotConceptual(notConceptual);
		field.setPrimaryKey(primaryKey);
		field.setUnique(unique);
		field.setNotNull(notNull);
		return field;
	}

	private static void seedClass(
			final PanelState panelState,
			final ClassModel classModel,
			final double x,
			final double y,
			final double width,
			final double height) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.CLASS);
		layout.setObjectId(classModel.getId());
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(width, height));
		panelState.getNodeLayouts().add(layout);
	}

	private static void seedComment(
			final PanelState panelState,
			final CommentModel commentModel,
			final double x,
			final double y,
			final double width,
			final double height) {
		final NodeLayout layout = new NodeLayout();
		layout.setObjectType(LayoutObjectType.COMMENT);
		layout.setObjectId(commentModel.getId());
		layout.setPosition(new Point2D.Double(x, y));
		layout.setSize(new Size2D(width, height));
		panelState.getNodeLayouts().add(layout);
	}

	private SampleDocumentFactory() {
	}

}
