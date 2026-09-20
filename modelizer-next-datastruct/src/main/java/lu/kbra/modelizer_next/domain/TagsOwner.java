package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;

public interface TagsOwner {

	String NOT_NULL_FLAG = "NN";
	String PRIMARY_KEY_FLAG = "PK";
	String UNIQUE_FLAG = "UQ";

	FieldTags getTags();

	void setTags(FieldTags tags);

	default boolean isPrimaryKey() {
		return this.getTags().isPrimaryKey();
	}

	default boolean isNonNull() {
		return this.getTags().isNonNull();
	}

	default boolean isUnique() {
		return this.getTags().isUnique();
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
			ll.add(TagsOwner.PRIMARY_KEY_FLAG);
		}
		if (this.isNonNull()) {
			ll.add(TagsOwner.NOT_NULL_FLAG);
		}
		if (this.isUnique()) {
			ll.add(TagsOwner.UNIQUE_FLAG);
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
