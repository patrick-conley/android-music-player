package pconley.vamp.model;

/**
 * A single value of a tag. Allows the tag to be associated to other matching
 * tracks.
 */
public class TagValue {

	private int id;
	private String value;

	public TagValue(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		TagValue other = (TagValue) obj;

		if (id != other.id)
			return false;
		if (value == null && other.value != null) {
			return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
