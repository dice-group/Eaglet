package org.aksw.gscheck.corrections;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class NamedEntityCorrections extends NamedEntity {
    public enum Check {
        INSERTED, DELETED, GOOD, NEED_TO_PAIR, OVERLAPS
    }

    private Check result;
    private NamedEntityCorrections partner;
    private String entity_name;
    private String doc;
    public String[] entity_text;
    private int number_of_lemma;

    public String getEntity_name() {
        return entity_name;
    }

    public void setEntity_name(String entity_name) {
        this.entity_name = entity_name;
    }

    public String getDoc() {
        return doc;
    }

    public int getNumber_of_lemma() {
        return number_of_lemma;
    }

    public void setNumber_of_lemma(int number_of_lemma) {
        this.number_of_lemma = number_of_lemma;
    }

    public void setDoc(String entity_text) {
        this.doc = entity_text;
    }

    public NamedEntityCorrections(int startPosition, int length, String uri) {
        super(startPosition, length, uri);
        // TODO Auto-generated constructor stub
        result = Check.GOOD;
        partner = null;

    }

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris) {
        super(startPosition, length, uris);
        // TODO Auto-generated constructor stub
        result = Check.GOOD;
        partner = null;
    }

    public NamedEntityCorrections(int startPosition, int length, String uri, Check result) {
        super(startPosition, length, uri);
        this.result = result;
    }

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, Check result) {
        super(startPosition, length, uris);
        this.result = result;
    }

    public NamedEntityCorrections getPartner() {
        return partner;
    }

    public void setPartner(NamedEntityCorrections pair_partner) {

        partner = pair_partner;
    }

    public Check getResult() {
        return result;
    }

    public void setResult(Check result) {
        this.result = result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((partner == null) ? 0 : partner.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
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
        NamedEntityCorrections other = (NamedEntityCorrections) obj;
        if (partner == null) {
            if (other.partner != null)
                return false;
        } else if (!partner.equals(other.partner))
            return false;
        if (result != other.result)
            return false;
        return true;
    }

}
