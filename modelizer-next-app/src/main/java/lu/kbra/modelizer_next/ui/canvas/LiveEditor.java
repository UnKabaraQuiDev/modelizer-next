package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
import lu.kbra.modelizer_next.domain.data.DisplayValueOwner;
import lu.kbra.modelizer_next.domain.shared.ElementStyle;
import lu.kbra.modelizer_next.layout.LayoutObjectType;
import lu.kbra.modelizer_next.layout.NodeLayout;
import lu.kbra.modelizer_next.layout.PanelType;
import lu.kbra.modelizer_next.style.StylePalette;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditComponents;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.LiveEditElement.LiveEditType;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingContext;
import lu.kbra.modelizer_next.ui.canvas.datastruct.SelectedElement.SelectedType;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.pclib.PCUtils;

public interface LiveEditor extends DiagramCanvasExt {

	default void editSelectionStyle(boolean alternative) {
		if (this.getCanvas().selectedElement == null || this.getCanvas().selectedElement.type() == SelectedType.NONE) {
			return;
		}

		System.err.println(getCanvas().selectedElement);
		invokeStyleEditingElement(getCanvas().selectedElement.asStyleEditElement(alternative));
	}

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

	default void invokeStyleEditingElement(final LiveEditElement element) {
		if (isLiveEditingElement()) {
			cancelLiveEditElement();
		}

		final DiagramCanvas canvas = this.getCanvas();

		canvas.liveEditElement = element;
		canvas.select(canvas.selectedElement);

		final JList<StylePalette> list = canvas.liveEditComponents.paletteList();
		final List<StylePalette> palettes = getFrame().getPalettes();

		SwingUtilities.invokeLater(() -> {
			list.setListData(palettes.toArray(StylePalette[]::new));
			list.setSelectedValue(canvas.defaultPalette, true);
//			list.setSelectedIndex(IntStream.range(0, palettes.size())
//					.filter(i -> palettes.get(i).getName().equals(App.CONFIG.getSelectedPaletteName()))
//					.findFirst()
//					.orElse(0));
			list.setFont(
					DiagramCanvas.BODY_FONT.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) getCanvas().getPanelState().getZoom()));
			list.setBounds(0, 0, 100, 100);

			list.setVisible(true);
			list.requestFocus();
			canvas.repaint();
		});
	}

	default void invokeRenamingElement(final LiveEditElement element) {
		if (this.isLiveEditingElement()) {
			cancelLiveEditElement();
		}

		final DiagramCanvas canvas = this.getCanvas();

		canvas.liveEditElement = element;
		canvas.select(element.asSelectedElement());

		final RenamingContext ctx = switch (element.type()) {
		case CLASS -> this.buildClassContext(element);
		case CLASS_FIELD -> this.buildClassFieldContext(element);
		case COMMENT -> this.buildCommentContext(element);
		case LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> this.buildLinkLabelContext(element);
		case LINK_TO_CARDINALITY, LINK_FROM_CARDINALITY -> this.buildLinkCardinalityContext(element);
		default -> throw new IllegalArgumentException("Unexpected value: " + element.type());
		};

		this.applyLiveEditContext(ctx);
	}

	default void updateLiveEditLayout() {
		if (!isLiveEditingElement()) {
			return;
		}

		final DiagramCanvas canvas = getCanvas();

		final LiveEditElement renamingElement = canvas.liveEditElement;
		final LiveEditComponents renamingComponents = canvas.liveEditComponents;

		final RenamingContext ctx = switch (renamingElement.type()) {
		case CLASS -> this.buildClassContext(renamingElement);
		case CLASS_FIELD -> this.buildClassFieldContext(renamingElement);
		case COMMENT -> this.buildCommentContext(renamingElement);
		case LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> this.buildLinkLabelContext(renamingElement);
		case LINK_TO_CARDINALITY, LINK_FROM_CARDINALITY -> this.buildLinkCardinalityContext(renamingElement);
		default -> throw new IllegalArgumentException("Unexpected value: " + renamingElement.type());
		};

		final JComponent comp = renamingElement.getRenamingComponent(renamingComponents);

		comp.setFont(DiagramCanvas.BODY_FONT.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) getCanvas().getPanelState().getZoom()));
		comp.setBounds((int) ctx.pos().getX(), (int) ctx.pos().getY(), (int) ctx.size().getX(), (int) ctx.size().getY());

		getCanvas().repaint();
	}

	@SuppressWarnings("unchecked")
	default void applyLiveEditContext(final RenamingContext ctx) {
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
				((JComboBox<Enum<?>>) comp).setModel(new DefaultComboBoxModel<>(enumClass.getEnumConstants()));
				((JComboBox<Enum<?>>) comp).setSelectedItem(ctx.value());
			} else {
				throw new IllegalArgumentException("Unsupported type: " + ctx.valueType());
			}

			comp.setFont(
					DiagramCanvas.BODY_FONT.deriveFont(DiagramCanvas.BODY_FONT.getSize() * (float) getCanvas().getPanelState().getZoom()));

			comp.setVisible(true);
			comp.requestFocus();
			if (comp instanceof final JTextComponent txt) {
				txt.selectAll();
			}
			canvas.repaint();
		});
	}

	default RenamingContext buildClassContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, e.classId());
		final ClassModel model = canvas.findClassById(e.classId());

		final Point2D pos = canvas.worldToScreen(nl.getPosition());
		@SuppressWarnings(
			"deprecation"
		) final Point2D size = canvas.worldToScreenZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_HEADER_HEIGHT));

		return new RenamingContext(pos, size, model.getNames().get(canvas.panelType), model.getStyle(), String.class, model);
	}

	default RenamingContext buildClassFieldContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, e.classId());
		final ClassModel classModel = canvas.findClassById(e.classId());
		final FieldModel field = canvas.findFieldById(e.classId(), e.fieldId());

		final int index = classModel.getFieldIndex(field.getId(), canvas.panelType);

		final Point2D fieldPos = new Point2D.Double(nl.getPosition().getX(),
				nl.getPosition().getY() + DiagramCanvas.CLASS_ROW_HEIGHT * (index + 1) + 6);

		@SuppressWarnings(
			"deprecation"
		) final Point2D size = canvas.worldToScreenZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_ROW_HEIGHT));

		return new RenamingContext(canvas
				.worldToScreen(fieldPos), size, field.getNames().get(canvas.panelType), field.getStyle(), String.class, field);
	}

	default RenamingContext buildCommentContext(final LiveEditElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, e.commentId());
		final CommentModel comment = canvas.findCommentById(e.commentId());

		final Point2D pos = canvas.worldToScreen(nl.getPosition());
		final Point2D size = canvas.worldToScreenZoom(nl.getSize());

		return new RenamingContext(pos, size, comment.getText(), comment.getStyle(), String.class, comment);
	}

	default RenamingContext buildLinkCardinalityContext(final LiveEditElement e) {
		final LinkModel linkModel = this.getCanvas().findLinkById(e.linkId());
		final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(linkModel);

		final Point2D pos;
		final Cardinality value;

		switch (e.type()) {
		case LINK_FROM_CARDINALITY -> {
			pos = this.getCanvas().worldToScreen(geometry.fromPoint());
			value = linkModel.getCardinalityFrom();
		}
		case LINK_TO_CARDINALITY -> {
			pos = this.getCanvas().worldToScreen(geometry.toPoint());
			value = linkModel.getCardinalityFrom();
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + e);
		}

		final Point2D size = new Point2D.Double(
				value == null ? 50 : this.getCanvas().liveEditComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().liveEditComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new RenamingContext(pos, size, value, new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK), Cardinality.class, linkModel);
	}

	default RenamingContext buildLinkLabelContext(final LiveEditElement e) {
		final LinkModel linkModel = this.getCanvas().findLinkById(e.linkId());
		final LinkGeometry geometry = this.getCanvas().resolveLinkGeometry(linkModel);

		final Point2D pos;
		final String value;

		switch (e.type()) {
		case LINK_LABEL -> {
			pos = this.getCanvas().worldToScreen(geometry.labelPoint());
			value = linkModel.getLabel();
		}
		case LINK_FROM_LABEL -> {
			pos = this.getCanvas().worldToScreen(geometry.fromPoint());
			value = linkModel.getLabelFrom();
		}
		case LINK_TO_LABEL -> {
			pos = this.getCanvas().worldToScreen(geometry.toPoint());
			value = linkModel.getLabelTo();
		}
		default -> throw new IllegalArgumentException("Unexpected type: " + e);
		}

		final Point2D size = new Point2D.Double(
				value == null || value.isBlank() ? 50 : this.getCanvas().liveEditComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().liveEditComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new RenamingContext(pos, size, value, new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK), String.class, linkModel);
	}

	default boolean isLiveEditingElement() {
		return this.getCanvas().liveEditElement != null;
	}

	default void cancelLiveEditElement() {
		if (!this.isLiveEditingElement()) {
			return;
		}
		this.getCanvas().liveEditComponents.setVisible(false);
		this.getCanvas().liveEditElement = null;
		this.getCanvas().repaint();
	}

	default void confirmRenamingElement(final int nextDir, final boolean alternative) {
		if (!this.isLiveEditingElement()) {
			this.getCanvas().liveEditComponents.setVisible(false);
			this.getCanvas().repaint();
			return;
		}

		final LiveEditElement liveEditElement = this.getCanvas().liveEditElement;
		final LiveEditComponents liveEditComponents = this.getCanvas().liveEditComponents;
		boolean next = nextDir != 0;

		System.err.println(liveEditElement);

		if (liveEditElement.type().isStyle()) {

			final StylePalette palette = liveEditComponents.paletteList().getSelectedValue();

			switch (liveEditElement.type()) {
			case CLASS_ALL_STYLE -> {
				final ClassModel classModel = this.getCanvas().findClassById(liveEditElement.classId());
				getCanvas().applyPaletteToClass(palette, classModel, true, false);
			}
			case CLASS_STYLE -> {
				final ClassModel classModel = this.getCanvas().findClassById(liveEditElement.classId());
				getCanvas().applyPaletteToClass(palette, classModel);
			}
			case CLASS_FIELD_STYLE -> {
				final FieldModel fieldModel = this.getCanvas().findFieldById(liveEditElement.classId(), liveEditElement.fieldId());
				getCanvas().applyPaletteToField(palette, fieldModel);
			}
			case COMMENT_STYLE -> {
				final CommentModel commentModel = this.getCanvas().findCommentById(liveEditElement.commentId());
				getCanvas().applyPaletteToComment(palette, commentModel);
			}
			case LINK_STYLE -> {
				final LinkModel linkModel = this.getCanvas().findLinkById(liveEditElement.linkId());
				getCanvas().applyPaletteToLink(palette, linkModel);
			}
			default -> new IllegalArgumentException("Unexpected type: " + liveEditElement);
			}

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
				linkModel.setCardinalityTo((Cardinality) liveEditComponents.enumComboBox().getSelectedItem());
			}
			case LINK_FROM_CARDINALITY -> {
				linkModel.setCardinalityFrom((Cardinality) liveEditComponents.enumComboBox().getSelectedItem());
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

	default LiveEditComponents createRenamingField() {
		final JTextField textField = new JTextField("editing");

		final JTextArea textArea = new JTextArea("editing");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		final JComboBox<Enum<?>> enumComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
		enumComboBox.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(
					final JList<?> list,
					final Object value,
					final int index,
					final boolean isSelected,
					final boolean cellHasFocus) {

				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof final DisplayValueOwner dvo) {
					this.setText(dvo.getDisplayValue());
				} else if (value instanceof final Enum<?> e) {
					this.setText(PCUtils.capitalize(e.name().toLowerCase().replace('_', ' ')));
				} else {
					this.setText(value != null ? value.toString() : "");
				}

				return this;
			}

		});

		final JList<StylePalette> stylePaletteList = new JList<>(new DefaultListModel<>());
		stylePaletteList.setCellRenderer(new StylePaletteRenderer());

		for (final JComponent renamingField : new JComponent[] { textField, textArea, enumComboBox, stylePaletteList }) {
			renamingField.setVisible(false);
			renamingField.setFocusTraversalKeysEnabled(false);
			renamingField.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(final FocusEvent e) {
					if (!e.isTemporary() && renamingField.isVisible() && e.getOppositeComponent() != renamingField) {
						SwingUtilities.invokeLater(() -> {
							if (!renamingField.hasFocus()) {
								getCanvas().cancelLiveEditElement();
							}
						});
					}
				}

			});
			renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
			renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
			renamingField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "submitNext");
			renamingField.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "submitPrevious");
			renamingField.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, MainFrame.CTRL_MODIFIER), "submitNextAlt");
			renamingField.getInputMap(JComponent.WHEN_FOCUSED)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK | MainFrame.CTRL_MODIFIER),
							"submitPreviousAlt");
			renamingField.getActionMap().put("cancel", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					getCanvas().cancelLiveEditElement();
				}

			});
			renamingField.getActionMap().put("submit", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(0, false);
				}

			});
			renamingField.getActionMap().put("submitNext", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(1, false);
				}

			});
			renamingField.getActionMap().put("submitPrevious", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(-1, false);
				}

			});
			renamingField.getActionMap().put("submitNextAlt", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(1, true);
				}

			});
			renamingField.getActionMap().put("submitPreviousAlt", new AbstractAction() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					LiveEditor.this.confirmRenamingElement(-1, true);
				}

			});
		}

		return new LiveEditComponents(textField, textArea, enumComboBox, stylePaletteList);
	}

}
