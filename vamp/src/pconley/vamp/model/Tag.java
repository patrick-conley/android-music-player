package pconley.vamp.model;

import java.util.HashSet;
import java.util.Set;

/**
 * An element of musical metadata.
 */
public class Tag {

	private String name;
	private Set<TagValue> values;

	/* Private constructor. Use the buildere. */
	private Tag() {}

	public String getName() {
		return name;
	}

	public Set<TagValue> getValues() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tag ").append(name).append(":\n");
		for (TagValue value : values) {
			sb.append("   ").append(value).append("\n");
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Tag other = (Tag) obj;
		if (name == null && other.name != null) {
			return false;
		} else if (!name.equals(other.name)) {
			return false;
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

	public static class Builder {

		private String name;
		private Set<TagValue> values;

		public Builder() {
			values = new HashSet<TagValue>();
		}

		public void name(String name) {
			this.name = name;
		}

		public void addValue(int id, String value) {
			this.values.add(new TagValue(id, value));
		}

		public Tag build() {
			Tag tag = new Tag();
			tag.name = name;
			tag.values = values;

			return tag;
		}
	}

}
