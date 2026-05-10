package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelState;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.ui.canvas.data.AnchorSide;
import lu.kbra.modelizer_next.ui.canvas.datastruct.DiagramCanvasActions;
import lu.kbra.modelizer_next.ui.canvas.datastruct.FieldAnchor;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

/**
 * Contains shared canvas helpers that do not own one specific feature area. It groups small
 * utilities, layout guards, link endpoint helpers, export filters, and UI notification helpers used
 * by the feature interfaces.
 */
interface DiagramCanvasCoreSupport extends DiagramCanvasExt {

	/**
	 * Appends the suffix on the active canvas.
	 * @param value value to process
	 * @param suffix suffix to append when needed
	 * @return the append suffix result
	 */
	default String appendSuffix(final String value, final String suffix) {
		if (value == null || value.isBlank()) {
			return value;
		}
		return value + suffix;
	}

	/**
	 * Constrains a numeric value to the supplied range.
	 * @param value value to process
	 * @param min numeric min value
	 * @param max numeric max value
	 * @return the clamp result
	 */
	default double clamp(final double value, final double min, final double max) {
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Returns the next anchor side in clockwise order.
	 * @param side node side to inspect
	 * @return the clockwise result
	 */
	default AnchorSide clockwise(final AnchorSide side) {
		return switch (side) {
		case TOP -> AnchorSide.RIGHT;
		case RIGHT -> AnchorSide.BOTTOM;
		case BOTTOM -> AnchorSide.LEFT;
		case LEFT -> AnchorSide.TOP;
		};
	}

	/**
	 * Configures the graphics on the active canvas.
	 * @param g2 graphics context used for drawing
	 */
	default void configureGraphics(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	/**
	 * Creates a graphics context on the active canvas.
	 * @return the created graphics context
	 */
	default Graphics2D createGraphicsContext() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return g2;
	}

	/**
	 * Ensures that the layouts exists or is up to date.
	 */
	default void ensureLayouts() {
		for (final ClassModel classModel : this.getCanvas().document.getModel().getClasses()) {
			if (classModel.isVisible(this.getPanelType())) {
				this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId());
			}
		}

		for (final CommentModel commentModel : this.getCanvas().document.getModel().getComments()) {
			if (this.getCanvas().isCommentVisible(commentModel)) {
				this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, commentModel.getId());
			}
		}
	}

	/**
	 * Ensures that the technical source field exists or is up to date.
	 * @param sourceClass source class value used by the operation
	 * @param targetClass target class value used by the operation
	 * @param targetField target field value used by the operation
	 * @return the ensure technical source field result
	 */
	default FieldModel ensureTechnicalSourceField(
			final ClassModel sourceClass,
			final ClassModel targetClass,
			final FieldModel targetField) {
		if (sourceClass == null || targetClass == null || targetField == null) {
			return null;
		}

		final String baseTechnicalName = this.getCanvas().buildForeignKeyFieldTechnicalName(targetClass, targetField);
		final String baseDisplayName = this.getCanvas().buildForeignKeyFieldName(targetClass, targetField);

		for (int suffix = 1; suffix < 1000; suffix++) {
			final String technicalName = suffix == 1 ? baseTechnicalName : baseTechnicalName + "_" + suffix;
			final String displayName = suffix == 1 ? baseDisplayName : baseDisplayName + "_" + suffix;

			FieldModel matchingField = null;
			for (final FieldModel fieldModel : sourceClass.getFields()) {
				if (fieldModel.isPrimaryKey()) {
					continue;
				}

				if (technicalName.equalsIgnoreCase(fieldModel.getTechnicalName())
						|| displayName.equalsIgnoreCase(fieldModel.getConceptualName())) {
					matchingField = fieldModel;
					break;
				}
			}

			if (matchingField != null) {
				if (!this.getCanvas().hasOutgoingTechnicalLink(sourceClass.getId(), matchingField.getId())) {
					return matchingField;
				}
				continue;
			}

			final FieldModel fieldModel = new FieldModel();
			fieldModel.setConceptualName(displayName);
			fieldModel.setTechnicalName(technicalName.isBlank() ? displayName : technicalName);
			fieldModel.setTechnicalOnly(true);
			fieldModel.setPrimaryKey(false);
			fieldModel.setUnique(false);
			fieldModel.setNotNull(false);
			fieldModel.setType(targetField.getType());
			this.getCanvas().applyDefaultPaletteToField(fieldModel);
			sourceClass.getFields().add(fieldModel);
			return fieldModel;
		}

		return null;
	}

	/**
	 * Finds the bound target anchor that matches the supplied input.
	 * @param g2 graphics context used for drawing
	 * @param commentModel comment model affected by the operation
	 * @return the matching bound target anchor, or {@code null} when no match exists
	 */
	default Point2D findBoundTargetAnchor(final Graphics2D g2, final CommentModel commentModel) {
		if (commentModel.getBinding() == null) {
			return null;
		}

		if (commentModel.getBinding().getTargetType() == BoundTargetType.CLASS) {
			final ClassModel classModel = this.getCanvas().findClassById(commentModel.getBinding().getTargetId());
			if (classModel == null || !classModel.isVisible(this.getPanelType())) {
				return null;
			}

			final NodeLayout layout = this.getCanvas()
					.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.getCanvas().computeClassBounds(classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		final LinkModel linkModel = this.getCanvas().findLinkById(commentModel.getBinding().getTargetId());
		final LinkGeometry geometry = linkModel == null ? null : this.getCanvas().resolveLinkGeometry(linkModel);
		return geometry == null ? null : geometry.middlePoint();
	}

	/**
	 * Returns the active links.
	 * @return the active links
	 */
	default List<LinkModel> getActiveLinks() {
		return this.getCanvas().panelType == PanelType.CONCEPTUAL ? this.getCanvas().document.getModel().getConceptualLinks()
				: this.getCanvas().document.getModel().getTechnicalLinks();
	}

	/**
	 * Returns the link creation source.
	 * @return the link creation source
	 */
	default SelectedElement getLinkCreationSource() {
		return this.getCanvas().linkCreationState == null ? null : this.getCanvas().linkCreationState.toSelectedElement();
	}

	/**
	 * Returns the technical side link count.
	 * @param classId id of the class to look up or modify
	 * @param side node side to inspect
	 * @param ignoredLinkId id of the element to read or modify
	 * @return the technical side link count
	 */
	default int getTechnicalSideLinkCount(final String classId, final AnchorSide side, final String ignoredLinkId) {
		if (classId == null || side != AnchorSide.LEFT && side != AnchorSide.RIGHT) {
			return 0;
		}

		int count = 0;
		for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
			if (linkModel == null || Objects.equals(linkModel.getId(), ignoredLinkId)) {
				continue;
			}
			if (classId.equals(linkModel.getFrom().getClassId())) {
				final AnchorSide endpointSide = this.getCanvas()
						.resolveTechnicalEndpointSide(linkModel.getFrom().getClassId(),
								linkModel.getFrom().getFieldId(),
								linkModel.getTo().getClassId(),
								linkModel.getTo().getFieldId(),
								linkModel.isSelfLinking());
				if (endpointSide == side) {
					count++;
				}
			}
			if (classId.equals(linkModel.getTo().getClassId())) {
				final AnchorSide endpointSide = this.getCanvas()
						.resolveTechnicalEndpointSide(linkModel.getTo().getClassId(),
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

	/**
	 * Returns the visible fields.
	 * @param classModel class model affected by the operation
	 * @return the visible fields
	 */
	@Deprecated
	default List<FieldModel> getVisibleFields(final ClassModel classModel) {
		final List<FieldModel> visibleFields = new ArrayList<>();

		for (final FieldModel fieldModel : classModel.getFields()) {
			if (this.getCanvas().panelType == PanelType.CONCEPTUAL && fieldModel.isTechnicalOnly()) {
				continue;
			}
			visibleFields.add(fieldModel);
		}

		return visibleFields;
	}

	/**
	 * Checks whether this object has an association class.
	 * @param linkModel link model affected by the operation
	 * @return {@code true} if association class exists; otherwise {@code false}
	 */
	default boolean hasAssociationClass(final LinkModel linkModel) {
		return linkModel != null && linkModel.getAssociationClassId() != null && !linkModel.getAssociationClassId().isBlank();
	}

	/**
	 * Checks whether this object has an outgoing technical link.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return {@code true} if outgoing technical link exists; otherwise {@code false}
	 */
	default boolean hasOutgoingTechnicalLink(final String classId, final String fieldId) {
		for (final LinkModel linkModel : this.getCanvas().document.getModel().getTechnicalLinks()) {
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

	/**
	 * Installs the key bindings on the active canvas.
	 */
	default void installKeyBindings() {
		this.getCanvas()
				.installDefaultKeyBindings(new DiagramCanvasActions(() -> this.getCanvas().renameSelection(false),
						() -> this.getCanvas().renameSelection(true),
						this.getCanvas()::moveFieldSelection,
						this.getCanvas()::moveSelectedFieldInList,
						this.getCanvas()::addTable,
						this.getCanvas()::addField,
						this.getCanvas()::addComment,
						this.getCanvas()::deleteSelection,
						this.getCanvas()::duplicateSelection,
						this.getCanvas()::clearSelection,
						this.getCanvas()::addLink,
						this.getCanvas()::selectAll,
						this.getCanvas()::editSelected,
						this.getCanvas()::copySelection,
						this.getCanvas()::cutSelection,
						this.getCanvas()::pasteSelection,
						this.getCanvas().documentEventListener::undo,
						this.getCanvas().documentEventListener::redo,
						() -> this.getCanvas().editSelectionStyle(false),
						() -> this.getCanvas().editSelectionStyle(true)));
	}

	/**
	 * Checks whether link connected to is enabled or applies.
	 * @param linkModel link model affected by the operation
	 * @param classId id of the class to look up or modify
	 * @return {@code true} if link connected to is enabled or applies; otherwise {@code false}
	 */
	default boolean isLinkConnectedTo(final LinkModel linkModel, final String classId) {
		if (linkModel == null || classId == null) {
			return false;
		}
		return linkModel.getFrom() != null && Objects.equals(linkModel.getFrom().getClassId(), classId)
				|| linkModel.getTo() != null && Objects.equals(linkModel.getTo().getClassId(), classId);
	}

	/**
	 * Checks whether valid preview target is enabled or applies on the active canvas.
	 * @param target target value used by the operation
	 * @return {@code true} if valid preview target is enabled or applies; otherwise {@code false}
	 */
	default boolean isValidPreviewTarget(final SelectedElement target) {
		if (target == null || this.getCanvas().linkCreationState == null) {
			return false;
		}

		final SelectedElement source = this.getCanvas().getLinkCreationSource();
		if (source == null) {
			return false;
		}

		if (source.type() == SelectedType.COMMENT) {
			return target.type() == SelectedType.CLASS || target.type() == SelectedType.LINK;
		}

		if (source.type() == SelectedType.CLASS && target.type() == SelectedType.LINK) {
			return this.getCanvas().findLinkById(target.linkId()) != null
					&& !this.getCanvas().isLinkConnectedTo(this.getCanvas().findLinkById(target.linkId()), source.classId());
		}

		if (this.getCanvas().panelType == PanelType.CONCEPTUAL) {
			return target.type() == SelectedType.CLASS;
		}

		final SelectedElement technicalTarget = this.getCanvas().resolveTechnicalTargetEndpoint(target);
		if (technicalTarget == null) {
			return false;
		}

		if (source.type() == SelectedType.FIELD) {
			if (Objects.equals(technicalTarget.classId(), source.classId())
					&& Objects.equals(technicalTarget.fieldId(), source.fieldId())) {
				return false;
			}

			return !this.getCanvas().hasOutgoingTechnicalLink(source.classId(), source.fieldId());
		}

		return source.type() == SelectedType.CLASS;
	}

	/**
	 * Maps the ID on the active canvas.
	 * @param idMap map to read or update
	 * @param oldId id of the element to read or modify
	 * @return the map ID result
	 */
	default String mapId(final Map<String, String> idMap, final String oldId) {
		if (oldId == null) {
			return null;
		}
		return idMap.getOrDefault(oldId, oldId);
	}

	/**
	 * Returns the mouse world pos on the active canvas.
	 * @return the mouse world pos
	 */
	default Point2D.Double getMouseWorldPos() {
		final Point mousePoint = this.getCanvas().getMousePosition();

		if (mousePoint == null) {
			return this.getCanvas().getViewportCenterWorld();
		}

		return this.getCanvas().screenToWorld(mousePoint);
	}

	/**
	 * Returns the mouse viewport pos on the active canvas.
	 * @return the mouse viewport pos
	 */
	default Point2D.Double getMouseViewportPos() {
		final Point mousePoint = this.getCanvas().getMousePosition();

		if (mousePoint != null) {
			return new Point2D.Double(mousePoint.x, mousePoint.y);
		}

		return this.getCanvas().getViewportCenter();
	}

	/**
	 * Normalizes the connection source selection.
	 * @param selection selection state to read or update
	 * @return the normalize connection source selection result
	 */
	default SelectedElement normalizeConnectionSourceSelection(final SelectedElement selection) {
		if (selection == null) {
			return null;
		}

		return switch (selection.type()) {
		case COMMENT -> SelectedElement.forComment(selection.commentId());
		case CLASS -> SelectedElement.forClass(selection.classId());
		case FIELD -> this.getCanvas().panelType == PanelType.CONCEPTUAL ? SelectedElement.forClass(selection.classId()) : selection;
		default -> null;
		};
	}

	/**
	 * Normalizes the connection target selection.
	 * @param selection selection state to read or update
	 * @return the normalize connection target selection result
	 */
	default SelectedElement normalizeConnectionTargetSelection(final SelectedElement selection) {
		if (selection == null || this.getCanvas().linkCreationState == null) {
			return null;
		}

		final SelectedElement source = this.getCanvas().getLinkCreationSource();
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

		if (this.getCanvas().panelType == PanelType.CONCEPTUAL) {
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

	/**
	 * Notifies the frame that the document content changed.
	 */
	default void notifyDocumentChanged() {
		this.getCanvas().invalidateConceptualAnchorCache();
		if (this.getCanvas().documentEventListener != null) {
			this.getCanvas().documentEventListener.onDocumentChanged();
		}
	}

	/**
	 * Notifies listeners that the selection changed changed.
	 */
	default void notifySelectionChanged() {
		if (this.getCanvas().documentEventListener != null) {
			this.getCanvas().documentEventListener.onSelectionChanged(this.getCanvas().getSelectionInfo());
		}
	}

	/**
	 * Opens the edit dialog for selection.
	 */
	default void openEditDialogForSelection() {
		if (this.getCanvas().selectedElement == null) {
			return;
		}

		switch (this.getCanvas().selectedElement.type()) {
		case FIELD -> this.getCanvas().editField(this.getCanvas().selectedElement.classId(), this.getCanvas().selectedElement.fieldId());
		case COMMENT -> this.getCanvas().editComment(this.getCanvas().selectedElement.commentId());
		case CLASS -> this.getCanvas().editClass(this.getCanvas().selectedElement.classId());
		case LINK -> this.getCanvas().editLink(this.getCanvas().selectedElement.linkId());
		default -> {
		}
		}
	}

	/**
	 * Resolves the conceptual preview anchor from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param reference reference value used by the operation
	 * @return the resolved conceptual preview anchor
	 */
	default Point2D resolveConceptualPreviewAnchor(final String classId, final Point2D reference) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return null;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D bounds = this.getCanvas().computeClassBounds(classModel, layout);

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

	/**
	 * Resolves the opposite reference point from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @return the resolved opposite reference point
	 */
	default Point2D resolveOppositeReferencePoint(final String classId, final String fieldId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return null;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.getCanvas().computeClassBounds(classModel, layout);

		if (this.getCanvas().panelType == PanelType.CONCEPTUAL || fieldId == null) {
			return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final double y = classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT
						+ DiagramCanvas.CLASS_ROW_HEIGHT / 2.0;
				return new Point2D.Double(classBounds.getCenterX(), y);
			}
		}

		return new Point2D.Double(classBounds.getCenterX(), classBounds.getCenterY());
	}

	/**
	 * Resolves the preview source anchor reference from the current model and layout state.
	 * @return the resolved preview source anchor reference
	 */
	default Point2D resolvePreviewSourceAnchorReference() {
		if (this.getCanvas().linkCreationState == null) {
			return this.getCanvas().linkPreviewMousePoint;
		}

		final SelectedElement source = this.getCanvas().getLinkCreationSource();
		if (source == null) {
			return this.getCanvas().linkPreviewMousePoint;
		}

		if (source.type() == SelectedType.COMMENT) {
			return this.getCanvas().resolveCommentCenterAnchor(source.commentId());
		}

		if (this.getCanvas().panelType == PanelType.CONCEPTUAL) {
			final ClassModel classModel = this.getCanvas().findClassById(source.classId());
			if (classModel == null || !classModel.isVisible(this.getPanelType())) {
				return this.getCanvas().linkPreviewMousePoint;
			}

			final NodeLayout layout = this.getCanvas()
					.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classModel.getId()));
			final Rectangle2D bounds = this.getCanvas().computeClassBounds(classModel, layout);
			return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
		}

		return this.getCanvas().resolveOppositeReferencePoint(source.classId(), source.fieldId());
	}

	/**
	 * Resolves the technical endpoint side from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @param oppositeClassId id of the element to read or modify
	 * @param oppositeFieldId id of the element to read or modify
	 * @param selfLink whether self link is enabled
	 * @return the resolved technical endpoint side
	 */
	default AnchorSide resolveTechnicalEndpointSide(
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId,
			final boolean selfLink) {
		if (selfLink) {
			return AnchorSide.LEFT;
		}

		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return AnchorSide.LEFT;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.getCanvas().computeClassBounds(classModel, layout);
		final Point2D oppositeReference = this.getCanvas().resolveOppositeReferencePoint(oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return AnchorSide.LEFT;
		}

		double centerX = classBounds.getCenterX();
		if (fieldId != null) {
			final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
			for (int i = 0; i < visibleFields.size(); i++) {
				if (visibleFields.get(i).getId().equals(fieldId)) {
					final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
							classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT,
							classBounds.getWidth(),
							DiagramCanvas.CLASS_ROW_HEIGHT);
					centerX = fieldBounds.getCenterX();
					break;
				}
			}
		}

		return oppositeReference.getX() <= centerX ? AnchorSide.LEFT : AnchorSide.RIGHT;
	}

	/**
	 * Resolves the technical field anchor from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @param oppositeReference opposite reference value used by the operation
	 * @return the resolved technical field anchor
	 */
	default Point2D resolveTechnicalFieldAnchor(final String classId, final String fieldId, final Point2D oppositeReference) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return null;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.getCanvas().computeClassBounds(classModel, layout);

		if (fieldId == null) {
			final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.CLASS_ROW_HEIGHT);
				final double x = oppositeReference.getX() < fieldBounds.getCenterX() ? fieldBounds.getX() : fieldBounds.getMaxX();
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		final double x = oppositeReference.getX() < classBounds.getCenterX() ? classBounds.getX() : classBounds.getMaxX();
		return new Point2D.Double(x, classBounds.getCenterY());
	}

	/**
	 * Resolves the technical field anchor from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @param oppositeClassId id of the element to read or modify
	 * @param oppositeFieldId id of the element to read or modify
	 * @return the resolved technical field anchor
	 */
	default FieldAnchor resolveTechnicalFieldAnchor(
			final String classId,
			final String fieldId,
			final String oppositeClassId,
			final String oppositeFieldId) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return null;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.getCanvas().computeClassBounds(classModel, layout);

		final Point2D oppositeReference = this.getCanvas().resolveOppositeReferencePoint(oppositeClassId, oppositeFieldId);
		if (oppositeReference == null) {
			return null;
		}

		if (fieldId == null) {
			final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
			final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
			return left.distance(oppositeReference) <= right.distance(oppositeReference) ? new FieldAnchor(left, AnchorSide.LEFT)
					: new FieldAnchor(right, AnchorSide.RIGHT);
		}

		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.CLASS_ROW_HEIGHT);

				final Point2D left = new Point2D.Double(fieldBounds.getX(), fieldBounds.getCenterY());
				final Point2D right = new Point2D.Double(fieldBounds.getMaxX(), fieldBounds.getCenterY());

				return left.distance(oppositeReference) <= right.distance(oppositeReference) ? new FieldAnchor(left, AnchorSide.LEFT)
						: new FieldAnchor(right, AnchorSide.RIGHT);
			}
		}

		final Point2D left = new Point2D.Double(classBounds.getX(), classBounds.getCenterY());
		final Point2D right = new Point2D.Double(classBounds.getMaxX(), classBounds.getCenterY());
		return left.distance(oppositeReference) <= right.distance(oppositeReference) ? new FieldAnchor(left, AnchorSide.LEFT)
				: new FieldAnchor(right, AnchorSide.RIGHT);
	}

	/**
	 * Resolves the technical self link anchor from the current model and layout state.
	 * @param classId id of the class to look up or modify
	 * @param fieldId id of the field to look up or modify
	 * @param side node side to inspect
	 * @return the resolved technical self link anchor
	 */
	default Point2D resolveTechnicalSelfLinkAnchor(final String classId, final String fieldId, final AnchorSide side) {
		final ClassModel classModel = this.getCanvas().findClassById(classId);
		if (classModel == null || !classModel.isVisible(this.getPanelType())) {
			return null;
		}

		final NodeLayout layout = this.getCanvas()
				.resolveRenderLayout(this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, classId));
		final Rectangle2D classBounds = this.getCanvas().computeClassBounds(classModel, layout);
		final double x = side == AnchorSide.LEFT ? classBounds.getX() : classBounds.getMaxX();

		if (fieldId == null) {
			return new Point2D.Double(x, classBounds.getCenterY());
		}

		final List<FieldModel> visibleFields = this.getCanvas().getVisibleFields(classModel);
		for (int i = 0; i < visibleFields.size(); i++) {
			if (visibleFields.get(i).getId().equals(fieldId)) {
				final Rectangle2D fieldBounds = new Rectangle2D.Double(classBounds.getX(),
						classBounds.getY() + DiagramCanvas.CLASS_HEADER_HEIGHT + i * DiagramCanvas.CLASS_ROW_HEIGHT,
						classBounds.getWidth(),
						DiagramCanvas.CLASS_ROW_HEIGHT);
				return new Point2D.Double(x, fieldBounds.getCenterY());
			}
		}

		return new Point2D.Double(x, classBounds.getCenterY());
	}

	/**
	 * Resolves the technical source endpoint from the current model and layout state.
	 * @param source source object used by the operation
	 * @param target target value used by the operation
	 * @return the resolved technical source endpoint
	 */
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

		final SelectedElement targetEndpoint = this.getCanvas().resolveTechnicalTargetEndpoint(target);
		if (targetEndpoint == null) {
			return null;
		}

		final ClassModel sourceClass = this.getCanvas().findClassById(source.classId());
		final ClassModel targetClass = this.getCanvas().findClassById(targetEndpoint.classId());
		final FieldModel targetField = this.getCanvas().findFieldById(targetEndpoint.classId(), targetEndpoint.fieldId());
		final FieldModel sourceField = this.getCanvas().ensureTechnicalSourceField(sourceClass, targetClass, targetField);
		return sourceField == null ? null : SelectedElement.forField(sourceClass.getId(), sourceField.getId());
	}

	/**
	 * Resolves the technical target endpoint from the current model and layout state.
	 * @param target target value used by the operation
	 * @return the resolved technical target endpoint
	 */
	default SelectedElement resolveTechnicalTargetEndpoint(final SelectedElement target) {
		if (target == null) {
			return null;
		}

		if (target.type() == SelectedType.FIELD) {
			final FieldModel fieldModel = this.getCanvas().findFieldById(target.classId(), target.fieldId());
			return fieldModel != null && fieldModel.isPrimaryKey() ? target : null;
		}

		if (target.type() != SelectedType.CLASS) {
			return null;
		}

		final FieldModel targetField = this.getCanvas().findPrimaryKeyField(target.classId());
		return targetField == null ? null : SelectedElement.forField(target.classId(), targetField.getId());
	}

	/**
	 * Converts screen coordinates into world coordinates.
	 * @param point point in canvas coordinates
	 * @return the screen to world result
	 */
	default Point2D.Double screenToWorld(final Point point) {
		final PanelState state = this.getCanvas().getPanelState();
		return new Point2D.Double((point.getX() - state.getPanX()) / state.getZoom(), (point.getY() - state.getPanY()) / state.getZoom());
	}

	/**
	 * Sets the association class for link.
	 * @param classId id of the class to look up or modify
	 * @param linkId id of the link to look up or modify
	 */
	default void setAssociationClassForLink(final String classId, final String linkId) {
		final LinkModel linkModel = this.getCanvas().findLinkById(linkId);
		if (classId == null || linkModel == null || this.getCanvas().findClassById(classId) == null
				|| this.getCanvas().isLinkConnectedTo(linkModel, classId)) {
			return;
		}
		final LinkModel alreadyExistingLinkModel = this.getCanvas().findLinkByAssociationClassId(classId);
		if (alreadyExistingLinkModel != null && alreadyExistingLinkModel.getAssociationClassId().equals(classId)) {
			alreadyExistingLinkModel.setAssociationClassId(null);
		}

		linkModel.setAssociationClassId(classId);
		this.getCanvas().select(SelectedElement.forLink(linkId));
		this.getCanvas().notifyDocumentChanged();
	}

	/**
	 * Decides whether the canvas should export class.
	 * @param classModel class model affected by the operation
	 * @return {@code true} if the operation should happen; otherwise {@code false}
	 */
	default boolean shouldExportClass(final ClassModel classModel) {
		if (this.getCanvas().exportSelectionFilter == null) {
			return true;
		}
		if (classModel == null) {
			return false;
		}

		for (final SelectedElement element : this.getCanvas().exportSelectionFilter) {
			if (element.type() == SelectedType.CLASS && Objects.equals(element.classId(), classModel.getId())
					|| element.type() == SelectedType.FIELD && Objects.equals(element.classId(), classModel.getId())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Decides whether the canvas should export comment.
	 * @param commentModel comment model affected by the operation
	 * @return {@code true} if the operation should happen; otherwise {@code false}
	 */
	default boolean shouldExportComment(final CommentModel commentModel) {
		return this.getCanvas().exportSelectionFilter == null || commentModel != null
				&& this.getCanvas().exportSelectionFilter.contains(SelectedElement.forComment(commentModel.getId()));
	}

	/**
	 * Decides whether the canvas should export link.
	 * @param linkModel link model affected by the operation
	 * @return {@code true} if the operation should happen; otherwise {@code false}
	 */
	default boolean shouldExportLink(final LinkModel linkModel) {
		return this.getCanvas().exportSelectionFilter == null
				|| linkModel != null && this.getCanvas().exportSelectionFilter.contains(SelectedElement.forLink(linkModel.getId()));
	}

	/**
	 * Returns the viewport center world on the active canvas.
	 * @return the viewport center world
	 */
	default Point2D.Double getViewportCenterWorld() {
		return worldToViewport(getViewportCenter());
	}

	/**
	 * Returns the viewport center on the active canvas.
	 * @return the viewport center
	 */
	default Point2D.Double getViewportCenter() {
		return new Point2D.Double(this.getCanvas().getWidth() / 2.0, this.getCanvas().getHeight() / 2.0);
	}

	/**
	 * Converts world coordinates into viewport coordinates.
	 * @param world world value used by the operation
	 * @return the world to viewport result
	 */
	default Point2D.Double worldToViewport(final Point2D world) {
		final PanelState state = this.getCanvas().getPanelState();
		return new Point2D.Double(world.getX() * state.getZoom() + state.getPanX(), world.getY() * state.getZoom() + state.getPanY());
	}

	/**
	 * Converts a world-space size into a zoomed viewport size.
	 * @param world world value used by the operation
	 * @return the world toviewport zoom result
	 */
	default Point2D.Double worldToviewportZoom(final Point2D world) {
		final PanelState state = this.getCanvas().getPanelState();
		return new Point2D.Double(world.getX() * state.getZoom(), world.getY() * state.getZoom());
	}

	/**
	 * Converts a world-space size into a zoomed viewport size.
	 * @param world world value used by the operation
	 * @return the world to viewport zoom result
	 */
	default Point2D.Double worldToViewportZoom(Dimension world) {
		final PanelState state = this.getCanvas().getPanelState();
		return new Point2D.Double(world.getWidth() * state.getZoom(), world.getHeight() * state.getZoom());
	}

	/**
	 * Wraps text into lines that fit within the requested width.
	 * @param text text to display or edit
	 * @param font font used for measurement or drawing
	 * @param maxWidth width value
	 * @return the matching values
	 */
	default List<String> wrapText(final String text, final Font font, final int maxWidth) {
		if (text == null || text.isEmpty()) {
			return Collections.emptyList();
		}

		final DiagramCanvas canvas = this.getCanvas();

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
				if (canvas.stringWidth(font, candidate) <= maxWidth) {
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
