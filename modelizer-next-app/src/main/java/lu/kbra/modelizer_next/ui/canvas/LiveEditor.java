package lu.kbra.modelizer_next.ui.canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
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
import lu.kbra.modelizer_next.ui.canvas.datastruct.LinkGeometry;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingComponents;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingContext;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingElement;
import lu.kbra.modelizer_next.ui.canvas.datastruct.RenamingElement.RenamingType;
import lu.kbra.modelizer_next.ui.frame.MainFrame;
import lu.kbra.pclib.PCUtils;

public interface LiveEditor extends DiagramCanvasExt {

	default void invokeRenamingElement(final RenamingElement element) {
		if (this.isRenamingElement()) {
			this.getCanvas().renamingComponents.setVisible(false);
			this.getCanvas().renamingElement = null;
		}

		final DiagramCanvas canvas = this.getCanvas();

		canvas.renamingElement = element;
		canvas.selectedElements.clear();
		canvas.select(element.asSelectedElement());

		final RenamingContext ctx = switch (element.type()) {
		case CLASS -> this.buildClassContext(element);
		case CLASS_FIELD -> this.buildClassFieldContext(element);
		case COMMENT -> this.buildCommentContext(element);
		case LINK_LABEL, LINK_TO_LABEL, LINK_FROM_LABEL -> this.buildLinkLabelContext(element);
		case LINK_TO_CARDINALITY, LINK_FROM_CARDINALITY -> this.buildLinkCardinalityContext(element);
		default -> throw new IllegalArgumentException("Unexpected value: " + element.type());
		};

		this.applyRenamingContext(ctx);
	}

	default void updateRenamingLayout() {
		if (!isRenamingElement()) {
			return;
		}

		final DiagramCanvas canvas = getCanvas();

		final RenamingElement renamingElement = canvas.renamingElement;
		final RenamingComponents renamingComponents = canvas.renamingComponents;

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
	default void applyRenamingContext(final RenamingContext ctx) {
		final DiagramCanvas canvas = this.getCanvas();

		final RenamingElement renamingElement = canvas.renamingElement;
		final RenamingComponents renamingComponents = canvas.renamingComponents;

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

	default RenamingContext buildClassContext(final RenamingElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.CLASS, e.classId());
		final ClassModel model = canvas.findClassById(e.classId());

		final Point2D pos = canvas.worldToScreen(nl.getPosition());
		@SuppressWarnings(
			"deprecation"
		) final Point2D size = canvas.worldToScreenZoom(new Point2D.Double(nl.getSize().getX(), DiagramCanvas.CLASS_HEADER_HEIGHT));

		return new RenamingContext(pos, size, model.getNames().get(canvas.panelType), model.getStyle(), String.class, model);
	}

	default RenamingContext buildClassFieldContext(final RenamingElement e) {
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

	default RenamingContext buildCommentContext(final RenamingElement e) {
		final var canvas = this.getCanvas();

		final NodeLayout nl = canvas.findOrCreateNodeLayout(LayoutObjectType.COMMENT, e.commentId());
		final CommentModel comment = canvas.findCommentById(e.commentId());

		final Point2D pos = canvas.worldToScreen(nl.getPosition());
		final Point2D size = canvas.worldToScreenZoom(nl.getSize());

		return new RenamingContext(pos, size, comment.getText(), comment.getStyle(), String.class, comment);
	}

	default RenamingContext buildLinkCardinalityContext(final RenamingElement e) {
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
				value == null ? 50 : this.getCanvas().renamingComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().renamingComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new RenamingContext(pos, size, value, new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK), Cardinality.class, linkModel);
	}

	default RenamingContext buildLinkLabelContext(final RenamingElement e) {
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
				value == null || value.isBlank() ? 50 : this.getCanvas().renamingComponents.textField().getPreferredSize().getWidth() + 10,
				this.getCanvas().renamingComponents.textField().getPreferredSize().getHeight());
		pos.setLocation(pos.getX() - size.getX() / 2, pos.getY() - size.getY() / 2);

		return new RenamingContext(pos, size, value, new ElementStyle(Color.BLACK, Color.WHITE, Color.BLACK), String.class, linkModel);
	}

	default boolean isRenamingElement() {
		return this.getCanvas().renamingElement != null;
	}

	default void cancelRenamingElement() {
		if (!this.isRenamingElement()) {
			return;
		}
		this.getCanvas().renamingComponents.setVisible(false);
		this.getCanvas().renamingElement = null;
		this.getCanvas().repaint();
	}

	default void confirmRenamingElement(final int nextDir, final boolean alternative) {
		if (!this.isRenamingElement()) {
			this.getCanvas().renamingComponents.setVisible(false);
			this.getCanvas().repaint();
			return;
		}

		final RenamingElement renamingElement = this.getCanvas().renamingElement;
		final RenamingComponents renamingComponents = this.getCanvas().renamingComponents;
		boolean next = nextDir != 0;

		if (renamingElement.type().isClass()) {

			final ClassModel classModel = this.getCanvas().findClassById(renamingElement.classId());
			switch (renamingElement.type()) {
			case CLASS -> {
				classModel.setName(this.getPanelType(), renamingElement.forceAlternative(), renamingComponents.textField().getText());

				if (next) {
					if (classModel.getFields().size() > 0) {
						this.getCanvas()
								.invokeRenamingElement(RenamingElement.forField(classModel.getId(),
										(nextDir < 0 ? classModel.getFields().getLast() : classModel.getFields().getFirst()).getId(),
										alternative));
					} else {
						next = false;
					}
				}
			}
			case CLASS_FIELD -> {
				final FieldModel fieldModel = this.getCanvas().findFieldById(renamingElement.classId(), renamingElement.fieldId());
				fieldModel.setName(this.getPanelType(), renamingElement.forceAlternative(), renamingComponents.textField().getText());

				if (next) {
					final int idx = classModel.getFieldIndex(fieldModel.getId(), this.getPanelType());
					if (idx + nextDir < 0 || idx + nextDir > classModel.getFieldCount(this.getPanelType()) - 1) {
						this.getCanvas().invokeRenamingElement(RenamingElement.forClass(classModel.getId(), alternative));
					} else {
						this.getCanvas()
								.invokeRenamingElement(RenamingElement.forField(classModel.getId(),
										classModel.getField(idx + nextDir, this.getPanelType()).getId(),
										alternative));
					}
				}
			}
			default -> new IllegalArgumentException("Unexpected type: " + renamingElement);
			}

		} else if (renamingElement.type().isLink() && this.getPanelType() == PanelType.CONCEPTUAL) {

			final LinkModel linkModel = this.getCanvas().findLinkById(renamingElement.linkId());
			switch (renamingElement.type()) {
			case LINK_LABEL -> {
				linkModel.setLabel(renamingComponents.textField().getText());
			}
			case LINK_TO_LABEL -> {
				linkModel.setLabelTo(renamingComponents.textField().getText());
			}
			case LINK_FROM_LABEL -> {
				linkModel.setLabelFrom(renamingComponents.textField().getText());
			}
			case LINK_TO_CARDINALITY -> {
				linkModel.setCardinalityTo((Cardinality) renamingComponents.comboBox().getSelectedItem());
			}
			case LINK_FROM_CARDINALITY -> {
				linkModel.setCardinalityFrom((Cardinality) renamingComponents.comboBox().getSelectedItem());
			}
			default -> new IllegalArgumentException("Unexpected type: " + renamingElement);
			}

			if (next) {
				this.getCanvas()
						.invokeRenamingElement(RenamingElement.forLink(linkModel.getId(),
								nextDir > 0 ? renamingElement.type().next() : renamingElement.type().previous()));
			}

		} else if (renamingElement.type().isLink() && this.getPanelType().isTechnical()) {

			final LinkModel linkModel = this.getCanvas().findLinkById(renamingElement.linkId());
			switch (renamingElement.type()) {
			case LINK_LABEL -> {
				linkModel.setLabel(renamingComponents.textField().getText());
			}
			default -> new IllegalArgumentException("Unexpected type: " + renamingElement);
			}

			next = false;
		} else if (renamingElement.type().isComment()) {

			final CommentModel commentModel = this.getCanvas().findCommentById(renamingElement.commentId());
			commentModel.setText(renamingComponents.textArea().getText());

			next = false;
		} else if (renamingElement.type() == RenamingType.NONE) {
			return;
		} else {
			throw new IllegalArgumentException("Unknown type: " + renamingElement);
		}

		this.getCanvas().notifyDocumentChanged();
		this.getCanvas().notifySelectionChanged();

		if (!next) {
			renamingComponents.setVisible(false);
			this.getCanvas().renamingElement = null;
			SwingUtilities.invokeLater(this.getCanvas()::requestFocusInWindow);
			this.getCanvas().repaint();
		}
	}

	default RenamingComponents createRenamingField() {
		final JTextField textField = new JTextField("editing");
		final JTextArea textArea = new JTextArea("editing");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		final JComboBox<Enum<?>> comboBox = new JComboBox<>(new DefaultComboBoxModel<>());
		comboBox.setRenderer(new DefaultListCellRenderer() {
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

		for (final JComponent renamingField : new JComponent[] { textField, textArea, comboBox }) {
			renamingField.setVisible(false);
			renamingField.setFocusTraversalKeysEnabled(false);
			renamingField.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(final FocusEvent e) {
					if (!e.isTemporary() && renamingField.isVisible() && e.getOppositeComponent() != renamingField) {
						SwingUtilities.invokeLater(() -> {
							if (!renamingField.hasFocus()) {
								getCanvas().cancelRenamingElement();
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
					getCanvas().cancelRenamingElement();
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

		return new RenamingComponents(textField, textArea, comboBox);
	}

}
