package lu.kbra.modelizer_next.ui.canvas;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

/**
 * Contains shared canvas helpers that do not own one specific feature area. It groups small
 * utilities, layout guards, link endpoint helpers, export filters, and UI notification helpers used
 * by the feature interfaces.
 */
interface DiagramCanvasCoreSupport extends DiagramCanvasExt {

	default String appendSuffix(final String value, final String suffix) {
		if (value == null || value.isBlank()) {
			return value;
		}
		return value + suffix;
	}

	default double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(max, value));
	}

	default AnchorSide clockwise(final AnchorSide side) {
		return switch (side) {
		case TOP -> AnchorSide.RIGHT;
		case RIGHT -> AnchorSide.BOTTOM;
		case BOTTOM -> AnchorSide.LEFT;
		case LEFT -> AnchorSide.TOP;
		};
	}

	default void configureGraphics(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	default Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
	}

	default void ensureLayouts() {
		for (final ClassModel classModel : getCanvas().document.getModel().getClasses()) {
			if (getCanvas().isVisible(classModel)) {
				getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			}
		}

		for (final CommentModel commentModel : getCanvas().document.getModel().getComments()) {
			final String text = getCanvas().resolveCommentText(commentModel);
			if (getCanvas().isCommentVisible(commentModel) && text != null && !text.isBlank()) {
				getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			}
		}
	}

	default FieldModel ensureTechnicalSourceField(
			final ClassModel sourceClass,
			final ClassModel targetClass,
			final FieldModel targetField) {
		if (sourceClass == null || targetClass == null || targetField == null) {
			return null;
		}

		final String baseTechnicalName = getCanvas().buildForeignKeyFieldTechnicalName(targetClass, targetField);
		final String baseDisplayName = getCanvas().buildForeignKeyFieldName(targetClass, targetField);

		for (int suffix = 1; suffix < 1000; suffix++) {
			final String technicalName = suffix == 1 ? baseTechnicalName : baseTechnicalName + "_" + suffix;
			final String displayName = suffix == 1 ? baseDisplayName : baseDisplayName + "_" + suffix;

			FieldModel matchingField = null;
			for (final FieldModel fieldModel : sourceClass.getFields()) {
				if (fieldModel.isPrimaryKey()) {
					continue;
				}

				if (technicalName.equalsIgnoreCase(fieldModel.getNames().getTechnicalName())
						|| displayName.equalsIgnoreCase(fieldModel.getNames().getConceptualName())) {
					matchingField = fieldModel;
					break;
				}
			}

			if (matchingField != null) {
				if (!getCanvas().hasOutgoingTechnicalLink(sourceClass.getId(), matchingField.getId())) {
					return matchingField;
				}
				continue;
			}

			final FieldModel fieldModel = new FieldModel();
			fieldModel.getNames().setConceptualName(displayName);
			fieldModel.getNames().setTechnicalName(technicalName.isBlank() ? displayName : technicalName);
			fieldModel.setNotConceptual(true);
			fieldModel.setPrimaryKey(false);
			fieldModel.setUnique(false);
			fieldModel.setNotNull(false);
			fieldModel.setType(targetField.getType());
			getCanvas().applyDefaultPaletteToField(fieldModel);
			sourceClass.getFields().add(fieldModel);
			return fieldModel;
		}

		return null;
	}

	default Point2D findBoundTargetAnchor(final Graphics2D g2, final CommentModel commentModel) {
		if (commentModel.getBinding() == null) {
			return null;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = getCanvas().findClassById(commentModel.getBinding().getTargetId());
			if (classModel == null || !getCanvas().isVisible(classModel)) {
				return null;
			}

			final NodeLayout layout = getCanvas()
					.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		final LinkModel linkModel = getCanvas().findLinkById(commentModel.getBinding().getTargetId());
		final LinkGeometry geometry = linkModel == null ? null : getCanvas().resolveLinkGeometry(g2, linkModel);
		return geometry == null ? null : geometry.middlePoint();
	}

	default List<LinkModel> getActiveLinks() {
		return getCanvas().panelType == PanelType.CONCEPTUAL ? getCanvas().document.getModel().getConceptualLinks()
				: getCanvas().document.getModel().getTechnicalLinks();
	}

	default SelectedElement getLinkCreationSource() {
		return getCanvas().linkCreationState == null ? null : getCanvas().linkCreationState.toSelectedElement();
	}

	default int getTechnicalSideLinkCount(final Graphics2D g2, final String classId, final AnchorSide side, final String ignoredLinkId) {
		if (classId == null || side != AnchorSide.LEFT && side != AnchorSide.RIGHT) {
			return 0;
		}

		int count = 0;
		for (final LinkModel linkModel : getCanvas().getActiveLinks()) {
			if (linkModel == null || Objects.equals(linkModel.getId(), ignoredLinkId)) {
				continue;
			}
			if (classId.equals(linkModel.getFrom().getClassId())) {
				final AnchorSide endpointSide = getCanvas().resolveTechnicalEndpointSide(g2,
						linkModel.getFrom().getClassId(),
						linkModel.getFrom().getFieldId(),
						linkModel.getTo().getClassId(),
						linkModel.getTo().getFieldId(),
						linkModel.isSelfLinking());
				if (endpointSide == side) {
					count++;
				}
			}
			if (classId.equals(linkModel.getTo().getClassId())) {
				final AnchorSide endpointSide = getCanvas().resolveTechnicalEndpointSide(g2,
						linkModel.getTo().getClassId(),
						linkModel.getTo().getFieldId(),
						linkModel.getFrom().getClassId(),
						linkModel.getFrom().getFieldId(),
						linkModel.isSelfLinking());
				if (endpointSide == side) {
					count++;
				}
			}
		}
		return count;
	}

	default List<FieldModel> getVisibleFields(final ClassModel classModel) {
		final List<FieldModel> visibleFields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (getCanvas().panelType == PanelType.CONCEPTUAL && fieldModel.isNotConceptual()) {
				continue;
			}
			visibleFields.add(fieldModel);
		}

		return visibleFields;
	}

	default boolean hasAssociationClass(final LinkModel linkModel) {
		return linkModel != null && linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank();
	}

	default boolean hasOutgoingTechnicalLink(final String classId, final String fieldId) {
		for (final LinkModel linkModel : getCanvas().document.getModel().getTechnicalLinks()) {
			if (linkModel.getFrom() == null || linkModel.getTo() == null || linkModel.getFrom().getFieldId() == null
					|| linkModel.getTo().getFieldId() == null) {
				continue;
			}

			if (Objects.equals(linkModel.getFrom().getClassId(), classId) && Objects.equals(linkModel.getFrom().getFieldId(), fieldId)) {
				return true;
			}
		}
		return false;
	}

	default void installKeyBindings() {
		DiagramCanvasActionRegistrar.installDefault(getCanvas(),
				new DiagramCanvasActionRegistrar.DiagramCanvasActions(getCanvas()::renameSelection,
						getCanvas()::moveFieldSelection,
						getCanvas()::moveSelectedFieldInList,
						getCanvas()::addTable,
						getCanvas()::addField,
						getCanvas()::addComment,
						getCanvas()::deleteSelection,
						getCanvas()::duplicateSelection,
						getCanvas()::clearSelection,
						getCanvas()::addLink,
						getCanvas()::selectAll,
						getCanvas()::editSelected,
						getCanvas()::copySelection,
						getCanvas()::cutSelection,
						getCanvas()::pasteSelection,
						getCanvas().documentEventListener::undo,
						getCanvas().documentEventListener::redo));
	}

	default boolean isLinkConnectedTo(final LinkModel linkModel, final String classId) {
		if (linkModel == null || classId == null) {
			return false;
		}
		return linkModel.getFrom() != null && Objects.equals(linkModel.getFrom().getClassId(), classId)
				|| linkModel.getTo() != null && Objects.equals(linkModel.getTo().getClassId(), classId);
	}

	default boolean isValidPreviewTarget(final SelectedElement target) {
		if (target == null || getCanvas().linkCreationState == null) {
			return false;
		}

		final SelectedElement source = getCanvas().getLinkCreationSource();
		if (source == null) {
			return false;
		}

		if (source.type() == SelectedType.COMMENT) {
			return target.type() == SelectedType.CLASS || target.type() == SelectedType.LINK;
		}

		if (source.type() == SelectedType.CLASS && target.type() == SelectedType.LINK) {
			return getCanvas().findLinkById(target.linkId()) != null
					&& !getCanvas().isLinkConnectedTo(getCanvas().findLinkById(target.linkId()), source.classId());
		}

		if (getCanvas().panelType == PanelType.CONCEPTUAL) {
			return target.type() == SelectedType.CLASS;
		}

		final SelectedElement technicalTarget = getCanvas().resolveTechnicalTargetEndpoint(target);
		if (technicalTarget == null) {
			return false;
		}

		if (source.type() == SelectedType.FIELD) {
			if (Objects.equals(technicalTarget.classId(), source.classId())
					&& Objects.equals(technicalTarget.fieldId(), source.fieldId())) {
				return false;
			}

			return !getCanvas().hasOutgoingTechnicalLink(source.classId(), source.fieldId());
		}

		return source.type() == SelectedType.CLASS;
	}

	default String mapId(final Map<String, String> idMap, final String oldId) {
		if (oldId == null) {
			return null;
		}
		return idMap.getOrDefault(oldId, oldId);
	}

	default Point2D.Double mouseWorldOrViewportCenter() {
		final Point mousePoint = getCanvas().getMousePosition();

		if (mousePoint == null) {
			return getCanvas().viewportCenterWorld();
		}

		return getCanvas().screenToWorld(mousePoint);
	}

	default SelectedElement normalizeConnectionSourceSelection(final SelectedElement selection) {
		if (selection == null) {
			return null;
		}

		return switch (selection.type()) {
		case COMMENT -> SelectedElement.forComment(selection.commentId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		case FIELD -> getCanvas().panelType == PanelType.CONCEPTUAL ? SelectedElement.forClass(selection.classId()) : selection;
		default -> null;
		};
	}

	default SelectedElement normalizeConnectionTargetSelection(final SelectedElement selection) {
		if (selection == null || getCanvas().linkCreationState == null) {
			return null;
		}

		final SelectedElement source = getCanvas().getLinkCreationSource();
		if (source == null) {
			return null;
		}

		if (source.type() == SelectedType.COMMENT) {
			return switch (selection.type()) {
			case CLASS, FIELD -> SelectedElement.forClass(selection.classId());
			case LINK -> SelectedElement.forLink(selection.linkId());
			default -> null;
			};
		}

		if (getCanvas().panelType == PanelType.CONCEPTUAL) {
			return switch (selection.type()) {
			case CLASS, FIELD -> SelectedElement.forClass(selection.classId());
			case LINK -> source.type() == SelectedType.CLASS ? SelectedElement.forLink(selection.linkId()) : null;
			default -> null;
			};
		}

		return switch (selection.type()) {
		case FIELD -> SelectedElement.forField(selection.classId(), selection.fieldId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		case LINK -> source.type() == SelectedType.CLASS ? SelectedElement.forLink(selection.linkId()) : null;
		default -> null;
		};
	}

	default void notifyDocumentChanged() {
		getCanvas().invalidateConceptualAnchorCache();
		if (getCanvas().documentEventListener != null) {
			getCanvas().documentEventListener.onDocumentChanged();
		}
	}

	default void notifySelectionChanged() {
		if (getCanvas().documentEventListener != null) {
			getCanvas().documentEventListener.onSelectionChanged(getCanvas().getSelectionInfo());
		}
	}

	default void openEditDialogForSelection() {
		if (getCanvas().selectedElement == null) {
			return;
		}

		switch (getCanvas().selectedElement.type()) {
		case FIELD -> getCanvas().editField(getCanvas().selectedElement.classId(), getCanvas().selectedElement.fieldId());
		case COMMENT -> getCanvas().editComment(getCanvas().selectedElement.commentId());
		case CLASS -> getCanvas().editClass(getCanvas().selectedElement.classId());
		case LINK -> getCanvas().editLink(getCanvas().selectedElement.linkId());
		default -> {
		}
		}
	}

	default Point2D resolveConceptualPreviewAnchor(final Graphics2D g2, final String classId, final Point2D reference) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);

		final Point2D effectiveReference = reference == null ? new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()) : reference;

		final List<Point2D> candidates = Arrays.asList(new Point2D.Double(bounds.getCenterX(), bounds.getY()),
				new Point2D.Double(bounds.getCenterX(), bounds.getMaxY()),
				new Point2D.Double(bounds.getX(), bounds.getCenterY()),
				new Point2D.Double(bounds.getMaxX(), bounds.getCenterY()));

		Point2D best = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (final Point2D candidate : candidates) {
			final double distance = candidate.distance(effectiveReference);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = candidate;
			}
		}

		return best;
	}

	default Point2D resolveOppositeReferencePoint(final Graphics2D g2, final String classId, final String fieldId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = getCanvas().computeClassBounds(g2, classModel, layout);

		if (getCanvas().panelType == PanelType.CONCEPTUAL || fieldId == null) {
			return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final double y = classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT
						+ DiagramCanvas.ROW_HEIGHT / 2.0;
				return new Point2D.Double(classBounds.getCenterX(), y);
			}
		}

		return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
	}

	default Point2D resolvePreviewSourceAnchorReference(final Graphics2D g2) {
		if (getCanvas().linkCreationState == null) {
			return getCanvas().linkPreviewMousePoint;
		}

		final SelectedElement source = getCanvas().getLinkCreationSource();
		if (source == null) {
			return getCanvas().linkPreviewMousePoint;
		}

		if (source.type() == SelectedType.COMMENT) {
			return getCanvas().resolveCommentCenterAnchor(g2, source.commentId());
		}

		if (getCanvas().panelType == PanelType.CONCEPTUAL) {
			final ClassModel classModel = getCanvas().findClassById(source.classId());
			if (classModel == null || !getCanvas().isVisible(classModel)) {
				return getCanvas().linkPreviewMousePoint;
			}

			final NodeLayout layout = getCanvas()
					.resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = getCanvas().computeClassBounds(g2, classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		return getCanvas().resolveOppositeReferencePoint(g2, source.classId(), source.fieldId());
	}

	default AnchorSide resolveTechnicalEndpointSide(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId,
			final boolean selfLink) {
		if (selfLink) {
			return AnchorSide.LEFT;
		}

		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return AnchorSide.LEFT;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = getCanvas().computeClassBounds(g2, classModel, layout);
		final Point2D oppositeReference = getCanvas().resolveOppositeReferencePoint(g2, oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return AnchorSide.LEFT;
		}

		double centerX = classBounds.getCenterX();
		if (fieldId != null) {
			final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
			for (int i = 0; i < visibleFields.size(); i++) {
				if (visibleFields.get(i).getId().equals(fieldId)) {
					final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
							classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
							classBounds.getWidth(),
							DiagramCanvas.ROW_HEIGHT);
					centerX = fieldBounds.getCenterX();
					break;
				}
			}
		}

		return oppositeReference.getX() <= centerX ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	default Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final Point2D oppositeReference) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = getCanvas().computeClassBounds(g2, classModel, layout);

		if (fieldId == null) {
			final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);
				final double x = oppositeReference.getX() < fieldBounds.getCenterX() ? fieldBounds.getX() : fieldBounds.getMaxX();
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
		return new Point2D.Double(x, classBounds.getCenterY());
	}

	default Point2D resolveTechnicalFieldAnchor(
			final Graphics2D g2,
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = getCanvas().computeClassBounds(g2, classModel, layout);

		final Point2D oppositeReference = getCanvas().resolveOppositeReferencePoint(g2, oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return null;
		}

		if (fieldId == null) {
			final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
			final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
			return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
		}

		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);

				final Point2D left = new Point2D.Double(fieldBounds.getX(), fieldBounds.getCenterY());
				final Point2D right = new Point2D.Double(fieldBounds.getMaxX(), fieldBounds.getCenterY());

				return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
			}
		}

		final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
		final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
		return left.distance(oppositeReference) <= right.distance(oppositeReference) ? left : right;
	}

	default Point2D resolveTechnicalSelfLinkAnchor(final Graphics2D g2, final String classId, final String fieldId, final AnchorSide side) {
		final ClassModel classModel = getCanvas().findClassById(classId);
		if (classModel == null || !getCanvas().isVisible(classModel)) {
			return null;
		}

		final NodeLayout layout = getCanvas().resolveRenderLayout(getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = getCanvas().computeClassBounds(g2, classModel, layout);
		final double x = side == AnchorSide.LEFT ? classBounds.getX() : classBounds.getMaxX();

		if (fieldId == null) {
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.HEADER_HEIGHT + i * DiagramCanvas.ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.ROW_HEIGHT);
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		return new Point2D.Double(x, classBounds.getCenterY());
	}

	default SelectedElement resolveTechnicalSourceEndpoint(final SelectedElement source, final SelectedElement target) {
		if (source == null || target == null) {
			return null;
		}

		if (source.type() == SelectedType.FIELD) {
			return source;
		}

		if (source.type() != SelectedType.CLASS) {
			return null;
		}

		final SelectedElement targetEndpoint = getCanvas().resolveTechnicalTargetEndpoint(target);
		if (targetEndpoint == null) {
			return null;
		}

		final ClassModel sourceClass = getCanvas().findClassById(source.classId());
		final ClassModel targetClass = getCanvas().findClassById(targetEndpoint.classId());
		final FieldModel targetField = getCanvas().findFieldById(targetEndpoint.classId(), targetEndpoint.fieldId());
		final FieldModel sourceField = getCanvas().ensureTechnicalSourceField(sourceClass, targetClass, targetField);
		return sourceField == null ? null : SelectedElement.forField(sourceClass.getId(), sourceField.getId());
	}

	default SelectedElement resolveTechnicalTargetEndpoint(final SelectedElement target) {
		if (target == null) {
			return null;
		}

		if (target.type() == SelectedType.FIELD) {
			final FieldModel fieldModel = getCanvas().findFieldById(target.classId(), target.fieldId());
			return fieldModel != null && fieldModel.isPrimaryKey() ? target : null;
		}

		if (target.type() != SelectedType.CLASS) {
			return null;
		}

		final FieldModel targetField = getCanvas().findPrimaryKeyField(target.classId());
		return targetField == null ? null : SelectedElement.forField(target.classId(), targetField.getId());
	}

	default Point2D.Double screenToWorld(final Point point) {
		final PanelState state = getCanvas().getPanelState();
		return new Point2D.Double((point.getX() - state.getPanX()) / state.getZoom(), (point.getY() - state.getPanY()) / state.getZoom());
	}

	default void setAssociationClassForLink(final String classId, final String linkId) {
		final LinkModel linkModel = getCanvas().findLinkById(linkId);
		if (classId == null || linkModel == null || getCanvas().findClassById(classId) == null
				|| getCanvas().isLinkConnectedTo(linkModel, classId)) {
			return;
		}
		final LinkModel alreadyExistingLinkModel = getCanvas().findLinkByAssociationClassId(classId);
		if (alreadyExistingLinkModel != null && alreadyExistingLinkModel.getAssociationClassId().equals(classId)) {
			alreadyExistingLinkModel.setAssociationClassId(null);
		}

		linkModel.setAssociationClassId(classId);
		getCanvas().select(SelectedElement.forLink(linkId));
		getCanvas().notifyDocumentChanged();
	}

	default boolean shouldExportClass(final ClassModel classModel) {
		if (getCanvas().exportSelectionFilter == null) {
			return true;
		}
		if (classModel == null) {
			return false;
		}

		for (final SelectedElement element : getCanvas().exportSelectionFilter) {
			if (element.type() == SelectedType.CLASS && Objects.equals(element.classId(), classModel.getId())
					|| element.type() == SelectedType.FIELD && Objects.equals(element.classId(), classModel.getId())) {
				return true;
			}
		}

		return false;
	}

	default boolean shouldExportComment(final CommentModel commentModel) {
		return getCanvas().exportSelectionFilter == null
				|| commentModel != null && getCanvas().exportSelectionFilter.contains(SelectedElement.forComment(commentModel.getId()));
	}

	default boolean shouldExportLink(final LinkModel linkModel) {
		return getCanvas().exportSelectionFilter == null
				|| linkModel != null && getCanvas().exportSelectionFilter.contains(SelectedElement.forLink(linkModel.getId()));
	}

	default Point2D.Double viewportCenterWorld() {
		final PanelState state = getCanvas().getPanelState();
		return new Point2D.Double((getCanvas().getWidth() / 2.0 - state.getPanX()) / state.getZoom(),
				(getCanvas().getHeight() / 2.0 - state.getPanY()) / state.getZoom());
	}

	default List<String> wrapText(final String text, final FontMetrics metrics, final int maxWidth) {
		final List<String> lines = new ArrayList<>();
		for (final String paragraph : text.split("\\R", -1)) {
			if (paragraph.isEmpty()) {
				lines.add("");
				continue;
			}

			final String[] words = paragraph.split(" ");
			final StringBuilder current = new StringBuilder();

			for (final String word : words) {
				if (current.isEmpty()) {
					current.append(word);
					continue;
				}

				final String candidate = current + " " + word;
				if (metrics.stringWidth(candidate) <= maxWidth) {
					current.append(" ").append(word);
				} else {
					lines.add(current.toString());
					current.setLength(0);
					current.append(word);
				}
			}

			lines.add(current.toString());
		}
		return lines;
	}

}
