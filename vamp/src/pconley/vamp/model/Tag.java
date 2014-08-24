package pconley.vamp.model;

import java.util.HashSet;
import java.util.Set;

/**
 * An element of musical metadata.
 */
public class Tag {

	private int id;
	private String name;
	private Set<String> values;

	public Tag(String name) {
		this.name = name;
		values = new HashSet<String>();
	}

	public Tag(int id, String name) {
		this(name);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<String> getValues() {
		return values;
	}

	public boolean addValue(String value) {
		return this.values.add(value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tag ").append(name).append(": ");
		for (String value : values) {
			sb.append(value).append("   ").append("\n");
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

}
