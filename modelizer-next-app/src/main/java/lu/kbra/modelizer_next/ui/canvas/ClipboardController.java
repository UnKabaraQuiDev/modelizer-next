package lu.kbra.modelizer_next.ui.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lu.kbra.modelizer_next.common.Size2D;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.BoundTargetType;
import lu.kbra.modelizer_next.domain.data.CommentKind;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;

/**
 * Contains clipboard actions for copying, cutting, duplicating, and pasting canvas selections.
 */
interface ClipboardController extends DiagramCanvasExt {

	default void copySelection() {
		if (this.getCanvas().selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.getCanvas().selectedElements);

		final Set<String> selectedClassIds = new LinkedHashSet<>();
		final Set<String> selectedFieldIds = new LinkedHashSet<>();
		final Set<String> selectedCommentIds = new LinkedHashSet<>();
		final Set<String> selectedLinkIds = new LinkedHashSet<>();

		for (final SelectedElement element : snapshot) {
			if (element.type() == SelectedType.CLASS) {
				selectedClassIds.add(element.classId());
			}
		}

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case FIELD -> {
				if (!selectedClassIds.contains(element.classId())) {
					selectedFieldIds.add(element.fieldId());
				}
			}
			case COMMENT -> selectedCommentIds.add(element.commentId());
			case LINK -> selectedLinkIds.add(element.linkId());
			default -> {
			}
			}
		}

		final List<CopiedClass> copiedClasses = new ArrayList<>();
		final List<CopiedField> copiedFields = new ArrayList<>();
		final List<CopiedComment> copiedComments = new ArrayList<>();
		final List<CopiedLink> copiedLinks = new ArrayList<>();

		final Set<String> copiedFieldIds = new HashSet<>();

		for (final String classId : selectedClassIds) {
			final ClassModel classModel = this.getCanvas().findClassById(classId);
			if (classModel == null) {
				continue;
			}

			for (final FieldModel fieldModel : classModel.getFields()) {
				copiedFieldIds.add(fieldModel.getId());
			}

			copiedClasses.add(this.getCanvas().captureClass(classModel));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.getCanvas().findOwnerClassOfField(fieldId);
			if (owner == null) {
				continue;
			}

			final FieldModel fieldModel = this.getCanvas().findFieldById(owner.getId(), fieldId);
			if (fieldModel == null) {
				continue;
			}

			copiedFieldIds.add(fieldModel.getId());
			copiedFields.add(this.getCanvas().captureField(owner.getId(), fieldModel));
		}

		final Set<String> linksToCopy = new LinkedHashSet<>(selectedLinkIds);

		for (final LinkModel linkModel : this.getCanvas().getActiveLinks()) {
			if (linkModel == null || linkModel.getFrom() == null || linkModel.getTo() == null) {
				continue;
			}

			final boolean touchesCopiedClass = selectedClassIds.contains(linkModel.getFrom().getClassId())
					|| selectedClassIds.contains(linkModel.getTo().getClassId())
					|| selectedClassIds.contains(linkModel.getAssociationClassId());

			final boolean touchesCopiedField = copiedFieldIds.contains(linkModel.getFrom().getFieldId())
					|| copiedFieldIds.contains(linkModel.getTo().getFieldId());

			if (touchesCopiedClass || touchesCopiedField) {
				linksToCopy.add(linkModel.getId());
			}
		}

		for (final String linkId : linksToCopy) {
			final LinkModel linkModel = this.getCanvas().findLinkById(linkId);
			if (linkModel != null) {
				copiedLinks.add(this.getCanvas().captureLink(linkModel));
			}
		}

		for (final String commentId : selectedCommentIds) {
			final CommentModel commentModel = this.getCanvas().findCommentById(commentId);
			if (commentModel != null) {
				copiedComments.add(this.getCanvas().captureComment(commentModel));
			}
		}

		DiagramCanvas.clipboardSnapshot = new ClipboardSnapshot(this.getPanelType(),
				List.copyOf(copiedClasses),
				List.copyOf(copiedFields),
				List.copyOf(copiedComments),
				List.copyOf(copiedLinks));
	}

	default void cutSelection() {
		this.copySelection();
		this.getCanvas().deleteSelection();
	}

	default void duplicateSelection() {
		if (this.getCanvas().selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.getCanvas().selectedElements);

		final Rectangle2D.Double selectionBounds = this.getCanvas().computeSelectionBounds(snapshot);
		final Point2D.Double duplicateTarget = this.getCanvas().mouseWorldOrViewportCenter();

		final double deltaX = selectionBounds == null ? DiagramCanvas.PASTE_OFFSET : duplicateTarget.getX() - selectionBounds.getCenterX();
		final double deltaY = selectionBounds == null ? DiagramCanvas.PASTE_OFFSET : duplicateTarget.getY() - selectionBounds.getCenterY();

		final Set<String> selectedClassIds = new HashSet<>();
		final Set<String> selectedFieldIds = new HashSet<>();
		final Set<String> selectedCommentIds = new HashSet<>();
		final Set<String> selectedLinkIds = new HashSet<>();

		for (final SelectedElement element : snapshot) {
			switch (element.type()) {
			case CLASS -> selectedClassIds.add(element.classId());
			case FIELD -> {
				if (!selectedClassIds.contains(element.classId())) {
					selectedFieldIds.add(element.fieldId());
				}
			}
			case COMMENT -> selectedCommentIds.add(element.commentId());
			case LINK -> selectedLinkIds.add(element.linkId());
			default -> {
			}
			}
		}

		final Map<String, String> duplicatedClassIds = new HashMap<>();
		final Map<String, String> duplicatedFieldIds = new HashMap<>();
		final Map<String, String> duplicatedCommentIds = new HashMap<>();
		final Map<String, String> duplicatedLinkIds = new HashMap<>();

		final LinkedHashSet<SelectedElement> newSelection = new LinkedHashSet<>();

		for (final String classId : selectedClassIds) {
			final ClassModel source = this.getCanvas().findClassById(classId);
			if (source == null) {
				continue;
			}

			final ClassModel copy = new ClassModel();
			copy.setConceptualName(source.getConceptualName() + " Copy");
			copy.setTechnicalName(source.getTechnicalName() + "_COPY");
			copy.setVisibleInConceptual(source.isVisibleInConceptual());
			copy.setVisibleInLogical(source.isVisibleInLogical());
			copy.setVisibleInPhysical(source.isVisibleInPhysical());
			copy.setTextColor(source.getTextColor());
			copy.setBackgroundColor(source.getBackgroundColor());
			copy.setBorderColor(source.getBorderColor());

			for (final FieldModel sourceField : source.getFields()) {
				final FieldModel fieldCopy = new FieldModel();
				fieldCopy.setConceptualName(sourceField.getConceptualName());
				fieldCopy.setTechnicalName(sourceField.getTechnicalName());
				fieldCopy.setTechnicalOnly(sourceField.isTechnicalOnly());
				fieldCopy.setPrimaryKey(sourceField.isPrimaryKey());
				fieldCopy.setUnique(sourceField.isUnique());
				fieldCopy.setNotNull(sourceField.isNotNull());
				fieldCopy.setTextColor(sourceField.getTextColor());
				fieldCopy.setBackgroundColor(sourceField.getBackgroundColor());
				copy.getFields().add(fieldCopy);

				duplicatedFieldIds.put(sourceField.getId(), fieldCopy.getId());
			}

			this.getCanvas().document.getModel().getClasses().add(copy);
			duplicatedClassIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, source.getId());
			final NodeLayout copyLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.CLASS, copy.getId());
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forClass(copy.getId()));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.getCanvas().findOwnerClassOfField(fieldId);
			if (owner == null || duplicatedClassIds.containsKey(owner.getId())) {
				continue;
			}

			final FieldModel source = this.getCanvas().findFieldById(owner.getId(), fieldId);
			if (source == null) {
				continue;
			}

			final FieldModel copy = new FieldModel();
			copy.setConceptualName(source.getConceptualName() + " Copy");
			copy.setTechnicalName(source.getTechnicalName() + "_COPY");
			copy.setTechnicalOnly(source.isTechnicalOnly());
			copy.setPrimaryKey(source.isPrimaryKey());
			copy.setUnique(source.isUnique());
			copy.setNotNull(source.isNotNull());
			copy.setTextColor(source.getTextColor());
			copy.setBackgroundColor(source.getBackgroundColor());

			final int insertIndex = owner.getFields().indexOf(source);
			if (insertIndex < 0) {
				owner.getFields().add(copy);
			} else {
				owner.getFields().add(insertIndex + 1, copy);
			}

			duplicatedFieldIds.put(source.getId(), copy.getId());
			newSelection.add(SelectedElement.forField(owner.getId(), copy.getId()));
		}

		for (final String commentId : selectedCommentIds) {
			final CommentModel source = this.getCanvas().findCommentById(commentId);
			if (source == null) {
				continue;
			}

			final CommentModel copy = new CommentModel();
			copy.setKind(source.getKind());
			copy.setText(source.getText());
			copy.setTextColor(source.getTextColor());
			copy.setBackgroundColor(source.getBackgroundColor());
			copy.setBorderColor(source.getBorderColor());

			if (source.getBinding() != null) {
				String targetId = source.getBinding().getTargetId();

				if (source.getBinding().getTargetType() == BoundTargetType.CLASS && duplicatedClassIds.containsKey(targetId)) {
					targetId = duplicatedClassIds.get(targetId);
				} else if (source.getBinding().getTargetType() == BoundTargetType.LINK && duplicatedLinkIds.containsKey(targetId)) {
					targetId = duplicatedLinkIds.get(targetId);
				}

				copy.setBinding(new CommentBinding(source.getBinding().getTargetType(), targetId));
			}

			this.getCanvas().document.getModel().getComments().add(copy);
			duplicatedCommentIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, source.getId());
			final NodeLayout copyLayout = this.getCanvas().findOrCreateNodeLayout(LayoutObjectType.COMMENT, copy.getId());
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forComment(copy.getId()));
		}

		final Set<String> linksToDuplicate = new LinkedHashSet<>(selectedLinkIds);
		for (final LinkModel link : this.getCanvas().getActiveLinks()) {
			final boolean fromClassDuplicated = duplicatedClassIds.containsKey(link.getFrom().getClassId());
			final boolean toClassDuplicated = duplicatedClassIds.containsKey(link.getTo().getClassId());
			final boolean fromFieldDuplicated = link.getFrom().getFieldId() != null
					&& duplicatedFieldIds.containsKey(link.getFrom().getFieldId());
			final boolean toFieldDuplicated = link.getTo().getFieldId() != null
					&& duplicatedFieldIds.containsKey(link.getTo().getFieldId());

			if (fromClassDuplicated || toClassDuplicated || fromFieldDuplicated || toFieldDuplicated) {
				linksToDuplicate.add(link.getId());
			}
		}

		for (final String linkId : linksToDuplicate) {
			final LinkModel source = this.getCanvas().findLinkById(linkId);
			if (source == null) {
				continue;
			}

			final String newFromClassId = duplicatedClassIds.getOrDefault(source.getFrom().getClassId(), source.getFrom().getClassId());
			final String newToClassId = duplicatedClassIds.getOrDefault(source.getTo().getClassId(), source.getTo().getClassId());
			final String newFromFieldId = source.getFrom().getFieldId() == null ? null
					: duplicatedFieldIds.getOrDefault(source.getFrom().getFieldId(), source.getFrom().getFieldId());
			final String newToFieldId = source.getTo().getFieldId() == null ? null
					: duplicatedFieldIds.getOrDefault(source.getTo().getFieldId(), source.getTo().getFieldId());

			final LinkModel copy = new LinkModel();
			copy.setName(source.getName());
			copy.setLineColor(source.getLineColor());
			copy.setAssociationClassId(source.getAssociationClassId() == null ? null
					: duplicatedClassIds.getOrDefault(source.getAssociationClassId(), source.getAssociationClassId()));
			copy.setFrom(new LinkEnd(newFromClassId, newFromFieldId));
			copy.setTo(new LinkEnd(newToClassId, newToFieldId));
			copy.setCardinalityFrom(source.getCardinalityFrom());
			copy.setCardinalityTo(source.getCardinalityTo());

			this.getCanvas().getActiveLinks().add(copy);
			duplicatedLinkIds.put(source.getId(), copy.getId());

			final LinkLayout sourceLayout = this.getCanvas().findOrCreateLinkLayout(source.getId());
			final LinkLayout copyLayout = this.getCanvas().findOrCreateLinkLayout(copy.getId());
			copyLayout.getBendPoints().clear();
			for (final Point2D.Double bendPoint : sourceLayout.getBendPoints()) {
				copyLayout.getBendPoints().add(new Point2D.Double(bendPoint.getX() + deltaX, bendPoint.getY() + deltaY));
			}
			if (sourceLayout.getNameLabelPosition() != null) {
				copyLayout.setNameLabelPosition(new Point2D.Double(sourceLayout.getNameLabelPosition().getX() + deltaX,
						sourceLayout.getNameLabelPosition().getY() + deltaY));
			}

			newSelection.add(SelectedElement.forLink(copy.getId()));
		}

		this.getCanvas().selectedElements.clear();
		this.getCanvas().selectedElements.addAll(newSelection);
		this.getCanvas().selectedElement = this.getCanvas().selectedElements.isEmpty() ? null : this.getCanvas().selectedElements.getLast();

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

	default void pasteSelection() {
		final ClipboardSnapshot clipboard = DiagramCanvas.clipboardSnapshot;

		if (clipboard == null || clipboard.isEmpty()) {
			return;
		}

		final Rectangle2D.Double clipboardBounds = this.getCanvas().computeClipboardBounds(clipboard);
		final Point2D.Double pasteTarget = this.getCanvas().mouseWorldOrViewportCenter();

		final double deltaX = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getX() - clipboardBounds.getCenterX();

		final double deltaY = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getY() - clipboardBounds.getCenterY();

		final Map<String, String> pastedClassIds = new HashMap<>();
		final Map<String, String> pastedFieldIds = new HashMap<>();
		final Map<String, String> pastedLinkIds = new HashMap<>();

		final LinkedHashSet<SelectedElement> newSelection = new LinkedHashSet<>();

		for (final CopiedClass copiedClass : clipboard.classes()) {
			final ClassModel classCopy = new ClassModel();

			classCopy.setConceptualName(this.getCanvas().appendSuffix(copiedClass.conceptualName(), " Copy"));
			classCopy.setTechnicalName(this.getCanvas().appendSuffix(copiedClass.technicalName(), "_COPY"));

			classCopy.setVisibleInConceptual(copiedClass.visibleInConceptual());
			classCopy.setVisibleInLogical(copiedClass.visibleInLogical());
			classCopy.setVisibleInPhysical(copiedClass.visibleInPhysical());

			classCopy.setTextColor(copiedClass.textColor());
			classCopy.setBackgroundColor(copiedClass.backgroundColor());
			classCopy.setBorderColor(copiedClass.borderColor());

			for (final CopiedField copiedField : copiedClass.fields()) {
				final FieldModel fieldCopy = this.getCanvas().createFieldFromClipboard(copiedField, false);
				classCopy.getFields().add(fieldCopy);
				pastedFieldIds.put(copiedField.sourceId(), fieldCopy.getId());
			}

			this.getCanvas().document.getModel().getClasses().add(classCopy);
			pastedClassIds.put(copiedClass.sourceId(), classCopy.getId());

			this.getCanvas().applyNodeLayout(LayoutObjectType.CLASS, classCopy.getId(), copiedClass.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forClass(classCopy.getId()));
		}

		for (final CopiedField copiedField : clipboard.fields()) {
			final String ownerClassId = this.getCanvas().mapId(pastedClassIds, copiedField.ownerClassId());
			final ClassModel owner = this.getCanvas().findClassById(ownerClassId);

			if (owner == null) {
				continue;
			}

			final FieldModel fieldCopy = this.getCanvas().createFieldFromClipboard(copiedField, true);

			int insertIndex = -1;
			for (int i = 0; i < owner.getFields().size(); i++) {
				if (Objects.equals(owner.getFields().get(i).getId(), copiedField.sourceId())) {
					insertIndex = i;
					break;
				}
			}

			if (insertIndex < 0) {
				owner.getFields().add(fieldCopy);
			} else {
				owner.getFields().add(insertIndex + 1, fieldCopy);
			}

			pastedFieldIds.put(copiedField.sourceId(), fieldCopy.getId());
			newSelection.add(SelectedElement.forField(owner.getId(), fieldCopy.getId()));
		}

		if (clipboard.panelType() == this.getPanelType()) {
			for (final CopiedLink copiedLink : clipboard.links()) {
				final LinkModel linkCopy = this.getCanvas().createLinkFromClipboard(copiedLink, pastedClassIds, pastedFieldIds);

				if (linkCopy == null) {
					continue;
				}

				this.getCanvas().getActiveLinks().add(linkCopy);
				pastedLinkIds.put(copiedLink.sourceId(), linkCopy.getId());

				this.getCanvas().applyLinkLayout(linkCopy.getId(), copiedLink.layout(), deltaX, deltaY);

				newSelection.add(SelectedElement.forLink(linkCopy.getId()));
			}
		}

		for (final CopiedComment copiedComment : clipboard.comments()) {
			final CommentModel commentCopy = new CommentModel();

			commentCopy.setKind(copiedComment.kind());
			commentCopy.setText(copiedComment.text());
			commentCopy.setTextColor(copiedComment.textColor());
			commentCopy.setBackgroundColor(copiedComment.backgroundColor());
			commentCopy.setBorderColor(copiedComment.borderColor());
			commentCopy.setVisibility(copiedComment.visibleInConceptual(),
					copiedComment.visibleInLogical(),
					copiedComment.visibleInPhysical());

			final CommentBinding binding = this.getCanvas().createRemappedCommentBinding(copiedComment, pastedClassIds, pastedLinkIds);
			if (binding != null) {
				commentCopy.setBinding(binding);
			} else if (copiedComment.kind() == CommentKind.BOUND) {
				commentCopy.setKind(CommentKind.STANDALONE);
			}

			this.getCanvas().document.getModel().getComments().add(commentCopy);
			this.getCanvas().applyNodeLayout(LayoutObjectType.COMMENT, commentCopy.getId(), copiedComment.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forComment(commentCopy.getId()));
		}

		if (newSelection.isEmpty()) {
			return;
		}

		this.getCanvas().selectedElements.clear();
		this.getCanvas().selectedElements.addAll(newSelection);
		this.getCanvas().selectedElement = this.getCanvas().selectedElements.getLast();

		this.getCanvas().document.getModel().getClasses().sort(this.getCanvas().comparator);

		this.getCanvas().notifySelectionChanged();
		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().repaint();
	}

}
