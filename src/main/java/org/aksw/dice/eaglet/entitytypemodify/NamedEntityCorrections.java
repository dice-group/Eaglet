package org.aksw.dice.eaglet.entitytypemodify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

/**
 * The class is a modified marking which stores error information.
 *
 * @author Kunal
 *
 */
public class NamedEntityCorrections extends NamedEntity {
    public enum Check {
        COMPLETED, INSERTED, DELETED, GOOD, NEED_TO_PAIR, OVERLAPS, INVALID_URI, DISAMBIG_URI, OUTDATED_URI
    }

    public enum DecisionValue {
        CORRECT, WRONG, ADDED, ADDEDBYUSER
    }

    public enum ErrorType {
        COMBINED, ERRATIC, LONGDESC, OVERLAPPING, WRONGPOSITION, INVALIDURIERR, DISAMBIGURIERR, OUTDATEDURIERR
    }

    private Check result;
    private NamedEntityCorrections partner;
    private String entity_name;
    private String doc;
    public String[] entity_text;
    private int number_of_lemma;
    private List<ErrorType> error = new ArrayList<ErrorType>();
    private DecisionValue userDecision;

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param des
     */
    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, DecisionValue des) {
        super(startPosition, length, uris);
        this.userDecision = des;
    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uri
     */

    public NamedEntityCorrections(int startPosition, int length, String uri) {
        super(startPosition, length, uri);
        // TODO Auto-generated constructor stub
        result = Check.GOOD;
        partner = null;

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param d
     */

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris,
            org.aksw.gerbil.transfer.nif.Document d) {
        super(startPosition, length, uris);
        // TODO Auto-generated constructor stub
        this.result = Check.GOOD;
        this.doc = d.getDocumentURI();
        this.partner = null;

        this.setEntity_name(d.getText().substring(startPosition, startPosition + length).toUpperCase());

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uri
     * @param result
     * @param partner
     */
    public NamedEntityCorrections(int startPosition, int length, String uri, Check result,
            NamedEntityCorrections partner) {
        super(startPosition, length, uri);
        this.result = result;
        this.partner = partner;

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     */
    public NamedEntityCorrections(int startPosition, int length, Set<String> uris) {
        super(startPosition, length, uris);
        // TODO Auto-generated constructor stub
        this.result = Check.GOOD;
        this.partner = null;

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uri
     * @param result
     */
    public NamedEntityCorrections(int startPosition, int length, String uri, Check result) {
        super(startPosition, length, uri);
        this.result = result;

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param result
     */
    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, Check result) {
        super(startPosition, length, uris);
        this.result = result;

    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param result
     * @param error
     */

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, Check result,
            List<ErrorType> error) {
        super(startPosition, length, uris);
        this.result = result;
        this.error = error;
    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param error
     */
    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, List<ErrorType> error) {
        super(startPosition, length, uris);

        this.error = error;
    }

    /**
     * Constructor
     *
     * @param startPosition
     * @param length
     * @param uris
     * @param error
     * @param userDescision
     */
    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, List<ErrorType> error,
            DecisionValue userDescision) {
        // TODO Auto-generated constructor stub
        super(startPosition, length, uris);
        this.error = error;
        this.userDecision = userDescision;

    }

    public NamedEntityCorrections(int startPosition, int length, Set<String> uris, List<ErrorType> error, Check result,
            DecisionValue userDecision) {
        super(startPosition, length, uris);
        this.result = result;
        this.error = error;
        this.userDecision = userDecision;
    }

    public String getEntity_name() {
        return entity_name;
    }

    public void setEntity_name(String entity_name) {
        this.entity_name = entity_name;
    }

    public DecisionValue getUserDecision() {
        return userDecision;
    }

    public void setUserDecision(DecisionValue userDecision) {
        this.userDecision = userDecision;
    }

    public List<ErrorType> getError() {
        return error;

    }

    public void setError(ErrorType error) {
        this.error.add(error);
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
        if (error != other.error)
            return false;
        if (this.userDecision != other.getUserDecision())
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(startPosition);
        builder.append(", ");
        builder.append(length);
        builder.append(", ");
        builder.append(Arrays.toString(uris.toArray()));
        builder.append(", ");
        builder.append(result);
        builder.append(", ");
        if (partner == null) {
            builder.append("noPartner");
        } else {
            builder.append('(');
            builder.append(partner.startPosition);
            builder.append(", ");
            builder.append(partner.length);
            builder.append(')');
        }
        builder.append(", ");
        builder.append(userDecision);
        builder.append(')');
        return builder.toString();
    }

}
