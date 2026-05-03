package lu.kbra.modelizer_next.domain.impl;

import lu.kbra.modelizer_next.domain.ElementNames;
import lu.kbra.modelizer_next.layout.PanelType;

public interface NamesOwner {

	default String getConceptualName() {
		return this.getNames().getConceptualName();
	}

	default String getName(final PanelType panelType) {
		return this.getNames().get(panelType);
	}

	ElementNames getNames();

	default String getTechnicalName() {
		return this.getNames().getTechnicalName();
	}

	default boolean hasTechnicalName() {
		return this.getNames().hasTechnicalName();
	}

	default void setConceptualName(final String name) {
		this.getNames().setConceptualName(name);
	}

	default void setName(final PanelType panelType, final String name) {
		this.getNames().set(panelType, name);
	}

	void setNames(ElementNames e);

	default void setTechnicalName(final String name) {
		this.getNames().setTechnicalName(name);
	}

}
