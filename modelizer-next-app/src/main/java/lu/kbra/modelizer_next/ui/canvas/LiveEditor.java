package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import lu.kbra.modelizer_next.domain.ClassModel;
import lu.kbra.modelizer_next.domain.CommentModel;
import lu.kbra.modelizer_next.domain.FieldModel;
import lu.kbra.modelizer_next.domain.LinkModel;
import lu.kbra.modelizer_next.domain.data.Cardinality;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditComponents;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditContext;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement.LiveEditType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;
import lu.kbra.pclib.datastructure.pair.Pair;
import lu.kbra.pclib.datastructure.pair.Pairs;

/**
 * Inline text editing support for class names, field names, and comment text on the canvas.
 */
public interface LiveEditor extends DiagramCanvasExt {

	/**
	 * Opens or applies editing for the selection style.
	 *
	 * @param alternative whether alternative is enabled
	 */
	default void editSelectionStyle(final boolean alternative) {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		this.invokeStyleEditingElement(this.getCanvas().selectedElement.asStyleEditElement(alternative,
				this.getStyleObject(this.getCanvas().selectedElement, alternative)));
	}

	/**
	 * Returns the style object on the active canvas.
	 *
	 * @param selectedElement selected element to read or update
	 * @param alternative     whether alternative is enabled
	 * @return the style object
	 */
	default Object getStyleObject(final SelectedElement selectedElement, final boolean alternative) {
		return switch (selectedElement.type()) {
		case CLASS -> alternative ? Pairs.readOnly(this.getCanvas().findClassById(selectedElement.classId()).getStyle().clone(),
				this.getCanvas()
						.findClassById(selectedElement.classId())
						.getFields()
						.stream()
						.map(FieldModel::getStyle)
						.map(ElementStyle::clone)
						.toList())
				: this.getCanvas().findClassById(selectedElement.classId()).getStyle().clone();
		case FIELD -> this.getCanvas().findFieldById(selectedElement.classId(), selectedElement.fieldId()).getStyle().clone();
		case COMMENT -> this.getCanvas().findCommentById(selectedElement.commentId()).getStyle().clone();
		case LINK -> this.getCanvas().findLinkById(selectedElement.linkId()).getLineColor();
		default -> throw new IllegalArgumentException("Unexpected value: " + selectedElement.type());
		};
	}

	/**
	 * Renames the selection.
	 *
	 * @param alternative whether alternative is enabled
	 */
	default void renameSelection(final boolean alternative) {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		switch (this.getCanvas().selectedElement.type()) {
		case CLASS, FIELD, COMMENT, LINK -> {
			this.getCanvas().invokeRenamingElement(this.getCanvas().selectedElement.asLiveEditElement(alternative));
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + this.getCanvas().selectedElement);
		}
	}

	/**
	 * Starts inline style editing for the selected element.
	 *
	 * @param element element value used by the operation
	 */
	default void invokeStyleEditingElement(final LiveEditElement element) {
		if (this.isLiveEditingElement()) {
			this.cancelLiveEditElement();
		}

		final DiagramCanvas canvas = this.getCanvas();

		canvas.liveEditElement = element;
		canvas.select(canvas.selectedElement);

		final JList<StylePalette> list = canvas.liveEditComponents.paletteList();
		final List<StylePalette> palettes = this.getFrame().getPalettes();

		SwingUtilities.invokeLater(() -> {
			((StylePaletteCellRenderer) list.getCellRenderer()).setScope(element.type().asSelectedType().asStyleScope());

			list.setListData(palettes.toArray(StylePalette[]::new));
			list.setSelectedValue(canvas.defaultPalette, true);
			list.setFont(DiagramCanvas.BODY_FONT
					.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) this.getCanvas().getPanelState().getZoom()));

			list.setFixedCellHeight(-1);
			final Dimension preferredSize = list.getPreferredSize();
			final Point2D.Double point = this.getCanvas().getMouseViewportPos();

			list.setBounds((int) point.getX() + 5,
					(int) point.getY(),
					(int) preferredSize.getWidth() + DiagramCanvas.TEXT_PADDING,
					(int) preferredSize.getHeight());

			list.setVisible(true);
			list.requestFocus();
			canvas.repaint();
		});
	}

	/**
	 * Starts inline renaming for the selected element.
	 *
	 * @param element element value used by the operation
	 */
	default void invokeRenamingElement(final LiveEditElement element) {
		if (this.isLiveEditingElement()) {
			this.cancelLiveEditElement();
		}

		final DiagramCanvas canvas = this.getCanvas();

		canvas.liveEditElement = element;
		canvas.select(element.asSelectedElement());

		final LiveEditContext ctx = switch (element.type()) {
		case CLASS -> this.buildClassContext(element);
		case CLASS_FIELD -> this.buildClassFieldContext(element);
		case COMMENT -> this.buildCommentContext(element);
		case LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> this.buildLinkLabelContext(element);
		case LINK_TO_CARDINALITY, LINK_FROM_CARDINALITY -> this.buildLinkCardinalityContext(element);
		default -> throw new IllegalArgumentException("Unexpected value: " + element.type());
		};

		this.applyLiveEditContext(ctx);
	}

	/**
	 * Updates the live edit layout.
	 */
	default void updateLiveEditLayout() {
		if (!this.isLiveEditingElement()) {
			return;
		}

		final DiagramCanvas canvas = this.getCanvas();

		final LiveEditElement liveEditElement = canvas.liveEditElement;
		final LiveEditComponents liveEditComponents = canvas.liveEditComponents;

		final JComponent comp = liveEditElement.getRenamingComponent(liveEditComponents);

		comp.setFont(
				DiagramCanvas.BODY_FONT.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) this.getCanvas().getPanelState().getZoom()));

		final Dimension preferredSize = comp.getPreferredSize();
		comp.setSize((int) (preferredSize.getWidth() + DiagramCanvas.TEXT_PADDING * this.getCanvas().getPanelState().getZoom()),
				(int) preferredSize.getHeight());

		if (liveEditElement.type().isStyle()) {
			this.getCanvas().repaint();
			return;
		}

		final LiveEditContext ctx = switch (liveEditElement.type()) {
		case CLASS -> this.buildClassContext(liveEditElement);
		case CLASS_FIELD -> this.buildClassFieldContext(liveEditElement);
		case COMMENT -> this.buildCommentContext(liveEditElement);
		case LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> this.buildLinkLabelContext(liveEditElement);
		case LINK_TO_CARDINALITY, LINK_FROM_CARDINALITY -> this.buildLinkCardinalityContext(liveEditElement);
		default -> throw new IllegalArgumentException("Unexpected value: " + liveEditElement.type());
		};

		comp.setLocation((int) ctx.pos().getX(), (int) ctx.pos().getY());
		if (ctx.fixedSize()) {
			comp.setSize((int) ctx.size().getX(), (int) ctx.size().getY());
		}

		this.getCanvas().repaint();
	}

	/**
	 * Updates the live edit preview on the active canvas.
	 */
	default void updateLiveEditPreview() {
		if (!this.isLiveEditingElement()) {
			return;
		}

		final DiagramCanvas canvas = this.getCanvas();

		final LiveEditElement renamingElement = canvas.liveEditElement;
		final LiveEditComponents renamingComponents = canvas.liveEditComponents;

		if (!renamingElement.type().isStyle()) {
			return;
		}

		final StylePalette palette = renamingComponents.paletteList().getSelectedValue();

		if (palette != null) {
			this.applyStyle(palette, renamingElement);
		}

		this.getCanvas().repaint();
	}

	/**
	 * Applies the live edit context on the active canvas.
	 *
	 * @param ctx ctx value used by the operation
	 */
	@SuppressWarnings("unchecked")
	default void applyLiveEditContext(final LiveEditContext ctx) {
		final DiagramCanvas canvas = this.getCanvas();

		final LiveEditElement renamingElement = canvas.liveEditElement;
		final LiveEditComponents renamingComponents = canvas.liveEditComponents;

		final JComponent comp = renamingElement.getRenamingComponent(renamingComponents);

		comp.setBounds((int) ctx.pos().getX(), (int) ctx.pos().getY(), (int) ctx.size().getX(), (int) ctx.size().getY());

		if (ctx.style() != null) {
			final ElementStyle style = ctx.style();
			comp.setBackground(style.getBackgroundColor());
			comp.setForeground(style.getTextColor());
			comp.setBorder(new CompoundBorder(new LineBorder(style.getBorderColor()),
					new EmptyBorder(0, DiagramCanvas.TEXT_PADDING, 0, DiagramCanvas.TEXT_PADDING)));
		} else {
			SwingUtilities.updateComponentTreeUI(comp);
		}

		SwingUtilities.invokeLater(() -> {
			if (ctx.valueType() == String.class) {
				((JTextComponent) comp).setText(ctx.value() == null ? "" : Objects.toString(ctx.value()));
			} else if (ctx.valueType().isEnum()) {
				final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) ctx.valueType().asSubclass(Enum.class);
				((JList<Enum<?>>) comp).setListData(enumClass.getEnumConstants());
				((JList<Enum<?>>) comp).setSelectedValue(ctx.value(), true);
			} else {
				throw new IllegalArgumentException("Unsupported type: " + ctx.valueType());
			}

			comp.setFont(DiagramCanvas.BODY_FONT
					.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) this.getCanvas().getPanelState().getZoom()));
			final Dimension preferredSize = comp.getPreferredSize();
			if (!ctx.fixedSize()) {
				comp.setSize((int) (preferredSize.getWidth() + DiagramCanvas.TEXT_PADDING * this.getCanvas().getPanelState().getZoom()),
						(int) preferredSize.getHeight());
			}

			comp.setVisible(true);
			comp.requestFocus();
			if (comp instanceof final JTextComponent txt) {
				txt.selectAll();
			}
			canvas.repaint();
		});
	}

	/**
	 * Builds a class context.
	 *
	 * @param e event object supplied by Swing
	 * @return the built class context
	 */
	default LiveEditContext buildClassContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, e.classId());
		final ClassModel model = canvas.findClassById(e.classId());

		final Point2D pos = canvas.worldToViewport(nl.getPosition());
		@SuppressWarnings(
			"deprecation"
		) final Point2D size = canvas.worldToviewportZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_HEADER_HEIGHT));

		return new LiveEditContext(pos, size, model.getNames().get(canvas.panelType), model.getStyle(), String.class, model, true);
	}

	/**
	 * Builds a class field context.
	 *
	 * @param e event object supplied by Swing
	 * @return the built class field context
	 */
	default LiveEditContext buildClassFieldContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, e.classId());
		final ClassModel classModel = canvas.findClassById(e.classId());
		final FieldModel field = canvas.findFieldById(e.classId(), e.fieldId());

		final int index = classModel.getFieldIndex(field.getId(), canvas.panelType);

		final Point2D fieldPos = new Point2D.Double(nl.getPosition().getX(),
				nl.getPosition().getY() + DiagramCanvas.CLASS_ROW_HEIGHT * (index + 1) + 6);

		@SuppressWarnings(
			"deprecation"
		) final Point2D size = canvas.worldToviewportZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_ROW_HEIGHT));

		return new LiveEditContext(canvas
				.worldToViewport(fieldPos), size, field.getNames().get(canvas.panelType), field.getStyle(), String.class, field, true);
	}

	/**
	 * Builds a comment context.
	 *
	 * @param e event object supplied by Swing
	 * @return the built comment context
	 */
	default LiveEditContext buildCommentContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, e.commentId());
		final CommentModel comment = canvas.findCommentById(e.commentId());

		final Point2D pos = canvas.worldToViewport(nl.getPosition());
		final Point2D size = canvas.worldToviewportZoom(nl.getSize());

		return new LiveEditContext(pos, size, comment.getText(), comment.getStyle(), String.class, comment, true);
	}

	/**
	 * Builds a link cardinality context.
	 *
	 * @param e event object supplied by Swing
	 * @return the built link cardinality context
	 */
	default LiveEditContext buildLinkCardinalityContext(final LiveEditElement e) {
		final LinkModel linkModel = this.getCanvas().findLinkById(e.linkId());
		final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(linkModel);

		final Point2D pos;
		final Cardinality value;

		switch (e.type()) {
		case LINK_FROM_CARDINALITY -> {
			pos = this.getCanvas().worldToViewport(geometry.fromPoint());
			value = linkModel.getCardinalityFrom();
		}
		case LINK_TO_CARDINALITY -> {
			pos = this.getCanvas().worldToViewport(geometry.toPoint());
			value = linkModel.getCardinalityFrom();
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + e);
		}

		final Point2D size = new Point2D.Double(
				value == null ? 50 : this.getCanvas().liveEditComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().liveEditComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new LiveEditContext(pos,
				size,
				value,
				new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK),
				Cardinality.class,
				linkModel,
				false);
	}

	/**
	 * Builds a link label context.
	 *
	 * @param e event object supplied by Swing
	 * @return the built link label context
	 */
	default LiveEditContext buildLinkLabelContext(final LiveEditElement e) {
		final LinkModel linkModel = this.getCanvas().findLinkById(e.linkId());
		final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(linkModel);

		final Point2D pos;
		final String value;

		switch (e.type()) {
		case LINK_LABEL -> {
			pos = this.getCanvas().worldToViewport(geometry.labelPoint());
			value = linkModel.getLabel();
		}
		case LINK_FROM_LABEL -> {
			pos = this.getCanvas().worldToViewport(geometry.fromPoint());
			value = linkModel.getLabelFrom();
		}
		case LINK_TO_LABEL -> {
			pos = this.getCanvas().worldToViewport(geometry.toPoint());
			value = linkModel.getLabelTo();
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + e);
		}

		final Point2D size = new Point2D.Double(
				value == null || value.isBlank() ? 50 : this.getCanvas().liveEditComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().liveEditComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new LiveEditContext(pos,
				size,
				value,
				new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK),
				String.class,
				linkModel,
				false);
	}

	/**
	 * Checks whether live editing element is enabled or applies on the active canvas.
	 *
	 * @return {@code true} if live editing element is enabled or applies; otherwise {@code false}
	 */
	default boolean isLiveEditingElement() {
		return this.getCanvas().liveEditElement != null;
	}

	/**
	 * Checks whether this object can cel live edit element on the active canvas.
	 */
	default void cancelLiveEditElement() {
		if (!this.isLiveEditingElement()) {
			return;
		}

		if (this.getCanvas().liveEditElement.type().isStyle()) {
			this.revertStyleObject(this.getCanvas().liveEditElement);
		}

		this.getCanvas().liveEditComponents.setVisible(false);
		this.getCanvas().liveEditElement = null;
		SwingUtilities.invokeLater(() -> {
			this.getCanvas().repaint();
			this.getCanvas().requestFocus();
		});
	}

	/**
	 * Restores the edited element style from a previously captured style object.
	 *
	 * @param liveEditElement live edit element value used by the operation
	 */
	default void revertStyleObject(final LiveEditElement liveEditElement) {
		final Object snapshotValue = liveEditElement.snapshotValue();
		final SelectedElement selectedElement = liveEditElement.asSelectedElement();

		switch (selectedElement.type()) {
		case CLASS -> {
			if (liveEditElement.forceAlternative()) {
				final Pair<ElementStyle, List<ElementStyle>> styles = (Pair<ElementStyle, List<ElementStyle>>) snapshotValue;
				final ClassModel classModel = this.getCanvas().findClassById(selectedElement.classId());
				classModel.setStyle(styles.getKey());
				for (int i = 0; i < classModel.getFields().size(); i++) {
					classModel.getFields().get(i).setStyle(styles.getValue().get(i));
				}
			} else {
				this.getCanvas().findClassById(selectedElement.classId()).setStyle((ElementStyle) snapshotValue);
			}
		}
		case FIELD -> {
			this.getCanvas().findFieldById(selectedElement.classId(), selectedElement.fieldId()).setStyle((ElementStyle) snapshotValue);
		}
		case COMMENT -> {
			this.getCanvas().findCommentById(selectedElement.commentId()).setStyle((ElementStyle) snapshotValue);
		}
		case LINK -> {
			this.getCanvas().findLinkById(selectedElement.linkId()).setLineColor((Color) snapshotValue);
		}
		}
	}

	/**
	 * Confirms whether the renaming element should continue on the active canvas.
	 *
	 * @param nextDir     numeric next dir value
	 * @param alternative whether alternative is enabled
	 */
	default void confirmRenamingElement(final int nextDir, final boolean alternative) {
		if (!this.isLiveEditingElement()) {
			this.getCanvas().liveEditComponents.setVisible(false);
			this.getCanvas().repaint();
			return;
		}

		final LiveEditElement liveEditElement = this.getCanvas().liveEditElement;
		final LiveEditComponents liveEditComponents = this.getCanvas().liveEditComponents;
		boolean next = nextDir != 0;

		if (liveEditElement.type().isStyle()) {

			final StylePalette palette = liveEditComponents.paletteList().getSelectedValue();

			this.applyStyle(palette, liveEditElement);

			next = false;

		} else if (liveEditElement.type().isClass()) {

			final ClassModel classModel = this.getCanvas().findClassById(liveEditElement.classId());
			switch (liveEditElement.type()) {
			case CLASS -> {
				classModel.setName(this.getPanelType(), liveEditElement.forceAlternative(), liveEditComponents.textField().getText());

				if (next) {
					if (classModel.getFields().size() > 0) {
						this.getCanvas()
								.invokeRenamingElement(LiveEditElement.forField(classModel.getId(),
										(nextDir < 0 ? classModel.getFields().getLast() : classModel.getFields().getFirst()).getId(),
										alternative));
					} else {
						next = false;
					}
				}
			}
			case CLASS_FIELD -> {
				final FieldModel fieldModel = this.getCanvas().findFieldById(liveEditElement.classId(), liveEditElement.fieldId());
				fieldModel.setName(this.getPanelType(), liveEditElement.forceAlternative(), liveEditComponents.textField().getText());

				if (next) {
					final int idx = classModel.getFieldIndex(fieldModel.getId(), this.getPanelType());
					if (idx + nextDir < 0 || idx + nextDir > classModel.getFieldCount(this.getPanelType()) - 1) {
						this.getCanvas().invokeRenamingElement(LiveEditElement.forClass(classModel.getId(), alternative));
					} else {
						this.getCanvas()
								.invokeRenamingElement(LiveEditElement.forField(classModel.getId(),
										classModel.getField(idx + nextDir, this.getPanelType()).getId(),
										alternative));
					}
				}
			}
			default -> new IllegalArgumentException("Unexpected type: " + liveEditElement);
			}

		} else if (liveEditElement.type().isLink() && this.getPanelType() == PanelType.CONCEPTUAL) {

			final LinkModel linkModel = this.getCanvas().findLinkById(liveEditElement.linkId());
			switch (liveEditElement.type()) {
			case LINK_LABEL -> {
				linkModel.setLabel(liveEditComponents.textField().getText());
			}
			case LINK_TO_LABEL -> {
				linkModel.setLabelTo(liveEditComponents.textField().getText());
			}
			case LINK_FROM_LABEL -> {
				linkModel.setLabelFrom(liveEditComponents.textField().getText());
			}
			case LINK_TO_CARDINALITY -> {
				linkModel.setCardinalityTo((Cardinality) liveEditComponents.enumList().getSelectedValue());
			}
			case LINK_FROM_CARDINALITY -> {
				linkModel.setCardinalityFrom((Cardinality) liveEditComponents.enumList().getSelectedValue());
			}
			default -> new IllegalArgumentException("Unexpected type: " + liveEditElement);
			}

			if (next) {
				this.getCanvas()
						.invokeRenamingElement(LiveEditElement.forLink(linkModel.getId(),
								nextDir > 0 ? liveEditElement.type().next() : liveEditElement.type().previous()));
			}

		} else if (liveEditElement.type().isLink() && this.getPanelType().isTechnical()) {

			final LinkModel linkModel = this.getCanvas().findLinkById(liveEditElement.linkId());
			switch (liveEditElement.type()) {
			case LINK_LABEL -> {
				linkModel.setLabel(liveEditComponents.textField().getText());
			}
			default -> new IllegalArgumentException("Unexpected type: " + liveEditElement);
			}

			next = false;
		} else if (liveEditElement.type().isComment()) {

			final CommentModel commentModel = this.getCanvas().findCommentById(liveEditElement.commentId());
			commentModel.setText(liveEditComponents.textArea().getText());

			next = false;
		} else if (liveEditElement.type() == LiveEditType.NONE) {
			return;
		} else {
			throw new IllegalArgumentException("Unknown type: " + liveEditElement);
		}

		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().notifySelectionChanged();

		if (!next) {
			liveEditComponents.setVisible(false);
			this.getCanvas().liveEditElement = null;
			SwingUtilities.invokeLater(this.getCanvas()::requestFocusInWindow);
			this.getCanvas().repaint();
		}
	}

	/**
	 * Applies the style on the active canvas.
	 *
	 * @param palette         palette value used by the operation
	 * @param liveEditElement live edit element value used by the operation
	 */
	default void applyStyle(final StylePalette palette, final LiveEditElement liveEditElement) {
		switch (liveEditElement.type()) {
		case CLASS_STYLE -> {
			final ClassModel classModel = this.getCanvas().findClassById(liveEditElement.classId());
			this.getCanvas().applyPaletteToClass(palette, classModel, liveEditElement.forceAlternative(), false);
		}
		case CLASS_FIELD_STYLE -> {
			final FieldModel fieldModel = this.getCanvas().findFieldById(liveEditElement.classId(), liveEditElement.fieldId());
			this.getCanvas().applyPaletteToField(palette, fieldModel);
		}
		case COMMENT_STYLE -> {
			final CommentModel commentModel = this.getCanvas().findCommentById(liveEditElement.commentId());
			this.getCanvas().applyPaletteToComment(palette, commentModel);
		}
		case LINK_STYLE -> {
			final LinkModel linkModel = this.getCanvas().findLinkById(liveEditElement.linkId());
			this.getCanvas().applyPaletteToLink(palette, linkModel);
		}
		default -> new IllegalArgumentException("Unexpected type: " + liveEditElement);
		}
	}

	/**
	 * Creates a renaming field.
	 *
	 * @return the created renaming field
	 */
	default LiveEditComponents createRenamingField() {
		final JTextField textField = new JTextField("editing");

		final JTextArea textArea = new JTextArea("editing");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		final JComboBox<Enum<?>> enumComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
		enumComboBox.setRenderer(new EnumCellRenderer());

		final JList<StylePalette> stylePaletteList = new JList<>(new DefaultListModel<>());
		stylePaletteList.setCellRenderer(new StylePaletteCellRenderer());
		stylePaletteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stylePaletteList.addListSelectionListener(e -> {

			if (!e.getValueIsAdjusting() && this.isLiveEditingElement() && this.getCanvas().liveEditElement.type().isStyle()) {
				this.applyStyle(stylePaletteList.getSelectedValue(), this.getCanvas().liveEditElement);

				this.getCanvas().repaint();
			}

		});

		final JList<Enum<?>> enumList = new JList<>(new DefaultListModel<>());
		enumList.setCellRenderer(new EnumCellRenderer());

		for (final JList<?> list : new JList[] { stylePaletteList, enumList }) {
			list.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseMoved(final MouseEvent e) {
					final int index = ((JList) e.getComponent()).locationToIndex(e.getPoint());

					if (index >= 0 && index != stylePaletteList.getSelectedIndex()) {
						((JList) e.getComponent()).setSelectedIndex(index);
					}
				}

			});
			list.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(final MouseEvent e) {

					if (!(e.getClickCount() >= 1 && SwingUtilities.isLeftMouseButton(e))) {
						return;
					}

					final int index = ((JList) e.getComponent()).locationToIndex(e.getPoint());
					if (index < 0) {
						return;
					}

					((JList) e.getComponent()).setSelectedIndex(index);
					final Action action = ((JList) e.getComponent()).getActionMap().get("submit");
					if (action != null) {
						action.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, "submit"));
					}
				}

			});
		}

		for (final JComponent component : new JComponent[] { textField, textArea, enumComboBox, stylePaletteList, enumList }) {
			component.setVisible(false);
			component.setFocusTraversalKeysEnabled(false);
			component.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(final FocusEvent e) {
					if (!e.isTemporary() && component.isVisible() && e.getOppositeComponent() != component) {
						SwingUtilities.invokeLater(() -> {
							if (!component.hasFocus()) {
								LiveEditor.this.getCanvas().cancelLiveEditElement();
							}
						});
					}
				}

			});
			component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
			component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
			component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "submitNext");
			component.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "submitPrevious");
			component.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.ALT_DOWN_MASK), "submitNextAlt");
			component.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
							"submitPreviousAlt");
			component.getActionMap().put("cancel", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.getCanvas().cancelLiveEditElement();
				}

			});
			component.getActionMap().put("submit", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(0, false);
				}

			});
			component.getActionMap().put("submitNext", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(1, false);
				}

			});
			component.getActionMap().put("submitPrevious", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(-1, false);
				}

			});
			component.getActionMap().put("submitNextAlt", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(1, true);
				}

			});
			component.getActionMap().put("submitPreviousAlt", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(-1, true);
				}

			});
		}

		return new LiveEditComponents(textField, textArea, enumComboBox, stylePaletteList, enumList);
	}

}
