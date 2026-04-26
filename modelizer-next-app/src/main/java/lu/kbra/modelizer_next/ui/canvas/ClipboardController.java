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
import lu.kbra.modelizer_next.domain.BoundTargetType;
import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentBinding;
import lu.kbra.modelizer_next.domain.CommentKind;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkEnd;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.LinkLayout;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.ui.canvas.datastruct.ClipboardSnapshot;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedClass;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedComment;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedField;
import lu.kbra.modelizer_next.ui.canvas.datastruct.CopiedLink;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedType;

final class ClipboardController {

	private final DiagramCanvasModuleRegistry registry;
	private final DiagramCanvas canvas;

	ClipboardController(final DiagramCanvasModuleRegistry registry, final DiagramCanvas canvas) {
		this.registry = Objects.requireNonNull(registry, "registry");
		this.canvas = Objects.requireNonNull(canvas, "canvas");
		this.registry.setClipboardController(this);
	}

	void copySelection() {
		if (this.canvas.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.canvas.selectedElements);

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
			final ClassModel classModel = this.canvas.findClassById(classId);
			if (classModel == null) {
				continue;
			}

			for (final FieldModel fieldModel : classModel.getFields()) {
				copiedFieldIds.add(fieldModel.getId());
			}

			copiedClasses.add(this.canvas.captureClass(classModel));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.canvas.findOwnerClassOfField(fieldId);
			if (owner == null) {
				continue;
			}

			final FieldModel fieldModel = this.canvas.findFieldById(owner.getId(), fieldId);
			if (fieldModel == null) {
				continue;
			}

			copiedFieldIds.add(fieldModel.getId());
			copiedFields.add(this.canvas.captureField(owner.getId(), fieldModel));
		}

		final Set<String> linksToCopy = new LinkedHashSet<>(selectedLinkIds);

		for (final LinkModel linkModel : this.canvas.getActiveLinks()) {
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
			final LinkModel linkModel = this.canvas.findLinkById(linkId);
			if (linkModel != null) {
				copiedLinks.add(this.canvas.captureLink(linkModel));
			}
		}

		for (final String commentId : selectedCommentIds) {
			final CommentModel commentModel = this.canvas.findCommentById(commentId);
			if (commentModel != null) {
				copiedComments.add(this.canvas.captureComment(commentModel));
			}
		}

		DiagramCanvas.clipboardSnapshot = new ClipboardSnapshot(this.canvas.panelType,
				List.copyOf(copiedClasses),
				List.copyOf(copiedFields),
				List.copyOf(copiedComments),
				List.copyOf(copiedLinks));
	}

	void cutSelection() {
		this.copySelection();
		this.canvas.deleteSelection();
	}

	void duplicateSelection() {
		if (this.canvas.selectedElements.isEmpty()) {
			return;
		}

		final List<SelectedElement> snapshot = new ArrayList<>(this.canvas.selectedElements);

		final Rectangle2D.Double selectionBounds = this.canvas.computeSelectionBounds(snapshot);
		final Point2D.Double duplicateTarget = this.canvas.mouseWorldOrViewportCenter();

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
			final ClassModel source = this.canvas.findClassById(classId);
			if (source == null) {
				continue;
			}

			final ClassModel copy = new ClassModel();
			copy.getNames().setConceptualName(source.getNames().getConceptualName() + " Copy");
			copy.getNames().setTechnicalName(source.getNames().getTechnicalName() + "_COPY");
			copy.setGroup(source.getGroup());
			copy.getVisibility().setConceptual(source.getVisibility().isConceptual());
			copy.getVisibility().setLogical(source.getVisibility().isLogical());
			copy.getVisibility().setPhysical(source.getVisibility().isPhysical());
			copy.getStyle().setTextColor(source.getStyle().getTextColor());
			copy.getStyle().setBackgroundColor(source.getStyle().getBackgroundColor());
			copy.getStyle().setBorderColor(source.getStyle().getBorderColor());

			for (final FieldModel sourceField : source.getFields()) {
				final FieldModel fieldCopy = new FieldModel();
				fieldCopy.getNames().setConceptualName(sourceField.getNames().getConceptualName());
				fieldCopy.getNames().setTechnicalName(sourceField.getNames().getTechnicalName());
				fieldCopy.setNotConceptual(sourceField.isNotConceptual());
				fieldCopy.setComment(sourceField.getComment());
				fieldCopy.setPrimaryKey(sourceField.isPrimaryKey());
				fieldCopy.setUnique(sourceField.isUnique());
				fieldCopy.setNotNull(sourceField.isNotNull());
				fieldCopy.getStyle().setTextColor(sourceField.getStyle().getTextColor());
				fieldCopy.getStyle().setBackgroundColor(sourceField.getStyle().getBackgroundColor());
				copy.getFields().add(fieldCopy);

				duplicatedFieldIds.put(sourceField.getId(), fieldCopy.getId());
			}

			this.canvas.document.getModel().getClasses().add(copy);
			duplicatedClassIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, source.getId());
			final NodeLayout copyLayout = this.canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, copy.getId());
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forClass(copy.getId()));
		}

		for (final String fieldId : selectedFieldIds) {
			final ClassModel owner = this.canvas.findOwnerClassOfField(fieldId);
			if (owner == null || duplicatedClassIds.containsKey(owner.getId())) {
				continue;
			}

			final FieldModel source = this.canvas.findFieldById(owner.getId(), fieldId);
			if (source == null) {
				continue;
			}

			final FieldModel copy = new FieldModel();
			copy.getNames().setConceptualName(source.getNames().getConceptualName() + " Copy");
			copy.getNames().setTechnicalName(source.getNames().getTechnicalName() + "_COPY");
			copy.setNotConceptual(source.isNotConceptual());
			copy.setComment(source.getComment());
			copy.setPrimaryKey(source.isPrimaryKey());
			copy.setUnique(source.isUnique());
			copy.setNotNull(source.isNotNull());
			copy.getStyle().setTextColor(source.getStyle().getTextColor());
			copy.getStyle().setBackgroundColor(source.getStyle().getBackgroundColor());

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
			final CommentModel source = this.canvas.findCommentById(commentId);
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

			this.canvas.document.getModel().getComments().add(copy);
			duplicatedCommentIds.put(source.getId(), copy.getId());

			final NodeLayout sourceLayout = this.canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, source.getId());
			final NodeLayout copyLayout = this.canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, copy.getId());
			copyLayout.setPosition(
					new Point2D.Double(sourceLayout.getPosition().getX() + deltaX, sourceLayout.getPosition().getY() + deltaY));
			copyLayout.setSize(new Size2D(sourceLayout.getSize().getWidth(), sourceLayout.getSize().getHeight()));

			newSelection.add(SelectedElement.forComment(copy.getId()));
		}

		final Set<String> linksToDuplicate = new LinkedHashSet<>(selectedLinkIds);
		for (final LinkModel link : this.canvas.getActiveLinks()) {
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
			final LinkModel source = this.canvas.findLinkById(linkId);
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

			this.canvas.getActiveLinks().add(copy);
			duplicatedLinkIds.put(source.getId(), copy.getId());

			final LinkLayout sourceLayout = this.canvas.findOrCreateLinkLayout(source.getId());
			final LinkLayout copyLayout = this.canvas.findOrCreateLinkLayout(copy.getId());
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

		this.canvas.selectedElements.clear();
		this.canvas.selectedElements.addAll(newSelection);
		this.canvas.selectedElement = this.canvas.selectedElements.isEmpty() ? null : this.canvas.selectedElements.getLast();

		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}

	void pasteSelection() {
		final ClipboardSnapshot clipboard = DiagramCanvas.clipboardSnapshot;

		if (clipboard == null || clipboard.isEmpty()) {
			return;
		}

		final Rectangle2D.Double clipboardBounds = this.canvas.computeClipboardBounds(clipboard);
		final Point2D.Double pasteTarget = this.canvas.mouseWorldOrViewportCenter();

		final double deltaX = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getX() - clipboardBounds.getCenterX();

		final double deltaY = clipboardBounds == null ? DiagramCanvas.PASTE_OFFSET : pasteTarget.getY() - clipboardBounds.getCenterY();

		final Map<String, String> pastedClassIds = new HashMap<>();
		final Map<String, String> pastedFieldIds = new HashMap<>();
		final Map<String, String> pastedLinkIds = new HashMap<>();

		final LinkedHashSet<SelectedElement> newSelection = new LinkedHashSet<>();

		for (final CopiedClass copiedClass : clipboard.classes()) {
			final ClassModel classCopy = new ClassModel();

			classCopy.getNames().setConceptualName(this.canvas.appendSuffix(copiedClass.conceptualName(), " Copy"));
			classCopy.getNames().setTechnicalName(this.canvas.appendSuffix(copiedClass.technicalName(), "_COPY"));
			classCopy.setGroup(copiedClass.group());

			classCopy.getVisibility().setConceptual(copiedClass.visibleInConceptual());
			classCopy.getVisibility().setLogical(copiedClass.visibleInLogical());
			classCopy.getVisibility().setPhysical(copiedClass.visibleInPhysical());

			classCopy.getStyle().setTextColor(copiedClass.textColor());
			classCopy.getStyle().setBackgroundColor(copiedClass.backgroundColor());
			classCopy.getStyle().setBorderColor(copiedClass.borderColor());

			for (final CopiedField copiedField : copiedClass.fields()) {
				final FieldModel fieldCopy = this.canvas.createFieldFromClipboard(copiedField, false);
				classCopy.getFields().add(fieldCopy);
				pastedFieldIds.put(copiedField.sourceId(), fieldCopy.getId());
			}

			this.canvas.document.getModel().getClasses().add(classCopy);
			pastedClassIds.put(copiedClass.sourceId(), classCopy.getId());

			this.canvas.applyNodeLayout(LayoutObjectType.CLASS, classCopy.getId(), copiedClass.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forClass(classCopy.getId()));
		}

		for (final CopiedField copiedField : clipboard.fields()) {
			final String ownerClassId = this.canvas.mapId(pastedClassIds, copiedField.ownerClassId());
			final ClassModel owner = this.canvas.findClassById(ownerClassId);

			if (owner == null) {
				continue;
			}

			final FieldModel fieldCopy = this.canvas.createFieldFromClipboard(copiedField, true);

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

		if (clipboard.panelType() == this.canvas.panelType) {
			for (final CopiedLink copiedLink : clipboard.links()) {
				final LinkModel linkCopy = this.canvas.createLinkFromClipboard(copiedLink, pastedClassIds, pastedFieldIds);

				if (linkCopy == null) {
					continue;
				}

				this.canvas.getActiveLinks().add(linkCopy);
				pastedLinkIds.put(copiedLink.sourceId(), linkCopy.getId());

				this.canvas.applyLinkLayout(linkCopy.getId(), copiedLink.layout(), deltaX, deltaY);

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

			commentCopy.setVisibleInConceptual(copiedComment.visibleInConceptual());
			commentCopy.setVisibleInLogical(copiedComment.visibleInLogical());
			commentCopy.setVisibleInPhysical(copiedComment.visibleInPhysical());

			final CommentBinding binding = this.canvas.createRemappedCommentBinding(copiedComment, pastedClassIds, pastedLinkIds);
			if (binding != null) {
				commentCopy.setBinding(binding);
			} else if (copiedComment.kind() == CommentKind.BOUND) {
				commentCopy.setKind(CommentKind.STANDALONE);
			}

			this.canvas.document.getModel().getComments().add(commentCopy);
			this.canvas.applyNodeLayout(LayoutObjectType.COMMENT, commentCopy.getId(), copiedComment.layout(), deltaX, deltaY);

			newSelection.add(SelectedElement.forComment(commentCopy.getId()));
		}

		if (newSelection.isEmpty()) {
			return;
		}

		this.canvas.selectedElements.clear();
		this.canvas.selectedElements.addAll(newSelection);
		this.canvas.selectedElement = this.canvas.selectedElements.getLast();

		this.canvas.document.getModel().getClasses().sort(this.canvas.comparator);

		this.canvas.notifySelectionChanged();
		this.canvas.notifyDocumentChanged();
		this.canvas.repaint();
	}
}
