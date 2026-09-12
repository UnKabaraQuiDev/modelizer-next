package lu.kbra.modelizer_next.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldTags {

	private boolean primaryKey;
	private boolean unique;
	@JsonAlias("notNull")
	private boolean nonNull;

	public FieldTags() {
		this.nonNull = true;
	}

}
