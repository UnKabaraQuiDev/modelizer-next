package lu.kbra.modelizer_next.json;

import java.awt.Dimension;
import java.util.Set;

import com.fasterxml.jackson.databind.module.SimpleModule;

import lu.kbra.modelizer_next.domain.data.PanelType;
import lu.kbra.modelizer_next.domain.data.ViewExportScope;

public final class CmdLineModule extends SimpleModule {

	@SuppressWarnings("unchecked")
	public CmdLineModule() {
		super("CmdLineModule");

		addDeserializer(Dimension.class, new DimensionDeserializer());

		addDeserializer((Class<Set<PanelType>>) (Class<?>) Set.class, new PanelTypeSetDeserializer());

		addDeserializer(ViewExportScope.class, new ViewExportScopeDeserializer());
	}

}