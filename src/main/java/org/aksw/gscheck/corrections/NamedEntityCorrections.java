package org.aksw.gscheck.corrections;

import java.util.ArrayList;
import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class NamedEntityCorrections extends NamedEntity {
    public enum Check {
        INSERTED, DELETED, GOOD, NEED_TO_PAIR, OVERLAPS
    }

    Check result;
    ArrayList<NamedEntityCorrections> partner;

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris) {
        super(startPosition, length, uris);
        // TODO Auto-generated constructor stub
        result = Check.GOOD;
        partner = null;

    }

    public ArrayList<NamedEntityCorrections> getPartner() {
        return partner;
    }

    public void setPartner(NamedEntityCorrections pair_partner) {
        if (partner == null) {
            partner = new ArrayList<NamedEntityCorrections>();
        }
        this.partner.add(pair_partner);
    }

    public Check getResult() {
        return result;
    }

    public void setResult(Check result) {
        this.result = result;
    }

}
