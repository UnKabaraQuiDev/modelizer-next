package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;

import lu.kbra.modelizer_next.ui.canvas.datastruct.FieldTags;

public interface TagsOwner {

	String NOT_NULL_FLAG = "NN";
	String PRIMARY_KEY_FLAG = "PK";
	String UNIQUE_FLAG = "UQ";

	FieldTags getTags();

	void setTags(FieldTags tags);

	default boolean isPrimaryKey() {
		return getTags().isPrimaryKey();
	}

	default boolean isNonNull() {
		return getTags().isNonNull();
	}

	default boolean isUnique() {
		return getTags().isUnique();
	}

	default void setPrimaryKey(final boolean primaryKey) {
		this.getTags().setPrimaryKey(primaryKey);
	}

	default void setNonNull(final boolean nonNull) {
		this.getTags().setNonNull(nonNull);
	}

	default void setUnique(final boolean unique) {
		this.getTags().setUnique(unique);
	}

	/**
	 * Returns the flags.
	 *
	 * @return the flags
	 */
	default List<String> getFlags() {
		final List<String> ll = new ArrayList<>();
		if (this.isPrimaryKey()) {
			ll.add(FieldModel.PRIMARY_KEY_FLAG);
		}
		if (this.isNonNull()) {
			ll.add(FieldModel.NOT_NULL_FLAG);
		}
		if (this.isUnique()) {
			ll.add(FieldModel.UNIQUE_FLAG);
		}
		return ll;
	}

	/**
	 * Checks whether this object has a flags.
	 *
	 * @return {@code true} if flags exists; otherwise {@code false}
	 */
	default boolean hasFlags() {
		return this.isPrimaryKey() || this.isNonNull() || this.isUnique();
	}

}
