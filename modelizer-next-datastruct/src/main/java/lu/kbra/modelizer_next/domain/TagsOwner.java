package lu.kbra.modelizer_next.domain;

import java.util.ArrayList;
import java.util.List;

public interface TagsOwner {

	String NOT_NULL_FLAG = "NN";
	String PRIMARY_KEY_FLAG = "PK";
	String UNIQUE_FLAG = "UQ";
	String AUTO_INCREMENT_FLAG = "AI";
	String GENERATED_FLAG = "GT";

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

	default boolean isAutoIncrement() {
		return this.getTags().isAutoIncrement();
	}

	default boolean isGenerated() {
		return this.getTags().isGenerated();
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

	default void setAutoIncrement(final boolean autoIncrement) {
		this.getTags().setAutoIncrement(autoIncrement);
	}

	default void setGenerated(final boolean generated) {
		this.getTags().setGenerated(generated);
	}

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
		if (this.isAutoIncrement()) {
			ll.add(TagsOwner.AUTO_INCREMENT_FLAG);
		}
		if (this.isGenerated()) {
			ll.add(TagsOwner.GENERATED_FLAG);
		}
		return ll;
	}

	default boolean hasFlags() {
		return this.isPrimaryKey() || this.isNonNull() || this.isUnique() || this.isAutoIncrement() || isGenerated();
	}

}
