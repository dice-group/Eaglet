package org.aksw.simba.eaglet.entitytypemodify;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class EntityCheck extends NamedEntity {

    protected boolean isNamedEntity;

    public EntityCheck(int startPosition, int length, Set<String> uris, boolean value) {
        super(startPosition, length, uris);
        this.isNamedEntity = value;
    }

    public boolean isNamedEntity() {
        return isNamedEntity;
    }

    public void setNamedEntity(boolean isNamedEntity) {
        this.isNamedEntity = isNamedEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isNamedEntity ? 1231 : 1237);
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
        if (isNamedEntity != other.isNamedEntity)
            return false;
        return true;
    }
}
