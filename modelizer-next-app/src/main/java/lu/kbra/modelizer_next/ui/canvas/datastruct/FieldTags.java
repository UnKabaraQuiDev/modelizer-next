package lu.kbra.modelizer_next.ui.canvas.datastruct;

import com.fasterxml.jackson.annotation.JsonAlias;

public class FieldTags {

	private boolean primaryKey;
	private boolean unique;
	@JsonAlias("notNull")
	private boolean nonNull;

	public FieldTags() {
		this.nonNull = true;
	}

	public FieldTags(boolean primaryKey, boolean unique, boolean nonNull) {
		this.primaryKey = primaryKey;
		this.unique = unique;
		this.nonNull = nonNull;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isNonNull() {
		return nonNull;
	}

	public void setNonNull(boolean nonNull) {
		this.nonNull = nonNull;
	}

	@Override
	public String toString() {
		return "FieldTagsData [primaryKey=" + primaryKey + ", unique=" + unique + ", nonNull=" + nonNull + "]";
	}

}
