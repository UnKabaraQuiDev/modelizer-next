package lu.kbra.modelizer_next.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldTags {

	private boolean primaryKey = false;
	private boolean unique = false;
	@JsonAlias("notNull")
	private boolean nonNull = true;
	private boolean autoIncrement = false;
	private boolean generated = false;

}
