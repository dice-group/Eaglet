package org.aksw.simba.eaglet.entitytypemodify;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class EntityCheck  extends NamedEntity {
	public boolean isNamedEntity() {
		return isNamedEntity;
	}
	public void setNamedEntity(boolean isNamedEntity) {
		this.isNamedEntity = isNamedEntity;
	}
	boolean isNamedEntity;
	public EntityCheck(int startPosition, int length, Set<String> uris, boolean value) {
		super(startPosition, length, uris);
		// TODO Auto-generated constructor stub
		this.isNamedEntity= value;
	}

}
