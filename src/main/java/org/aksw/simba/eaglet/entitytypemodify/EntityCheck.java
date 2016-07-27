package org.aksw.simba.eaglet.entitytypemodify;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class EntityCheck extends NamedEntity {

	protected boolean isMarkedCorrect;
	protected boolean isMarkedWrong;
	protected boolean isMarkedMissing;
	protected boolean isMarkedAdded;

	public EntityCheck(int startPosition, int length, Set<String> uris,
			boolean correctValue, boolean wrongValue, boolean missingValue,
			boolean addedValue) {
		super(startPosition, length, uris);
		this.isMarkedCorrect = correctValue;
		this.isMarkedWrong = wrongValue;
		this.isMarkedMissing = missingValue;
		this.isMarkedAdded = addedValue;
	}

	public boolean isCorrect() {
		return isMarkedCorrect;
	}

	public void setCorrect(boolean correct) {
		this.isMarkedCorrect = correct;
	}

	public boolean isWrong() {
		return isMarkedWrong;
	}

	public void setWrong(boolean wrong) {
		this.isMarkedWrong = wrong;
	}

	public boolean isMissing() {
		return isMarkedMissing;
	}

	public void setMissing(boolean missing) {
		this.isMarkedMissing = missing;
	}

	public boolean isAdded() {
		return isMarkedAdded;
	}

	public void setAdded(boolean added) {
		this.isMarkedAdded = added;
	}

	// TODO: Check Hashcode.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isMarkedCorrect ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityCheck other = (EntityCheck) obj;
		if (isMarkedCorrect != other.isMarkedCorrect)
			return false;
		if (isMarkedWrong != other.isMarkedWrong)
			return false;
		if (isMarkedMissing != other.isMarkedMissing)
			return false;
		if (isMarkedAdded != other.isMarkedAdded)
			return false;
		return true;
	}
}
