package org.aksw.dice.eaglet.entitytypemodify;

import java.util.Arrays;
import java.util.Set;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

/**
 * The class is a modified marking which stores error information.
 *
 * @author Kunal
 *
 */
public class NamedEntityCorrections extends NamedEntity {
	public enum Correction {
		INSERT, DELETE, GOOD, CHECK, NOTDEFINED
	}

	public enum DecisionValue {
		CORRECT, WRONG, ADDED, ADDEDBYUSER
	}

	public enum ErrorType {
		NOERROR, COMBINEDTAGGINGERR, INCONSITENTMARKINGERR, LONGDESCERR, OVERLAPPINGERR, WRONGPOSITIONERR, INVALIDURIERR, DISAMBIGURIERR, OUTDATEDURIERR
	}

	private Correction correctionSuggested;
	private NamedEntityCorrections partner;
	private String entity_name;
	private String doc;
	public String[] entity_text;
	private int number_of_lemma;
	private ErrorType error;
	private DecisionValue userDecision;

	/**
	 * Constructor Type 1
	 *
	 * @param startPosition
	 * @param length
	 * @param uris
	 * @param des
	 */
	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, DecisionValue des) {
		super(startPosition, length, uris);
		this.error = ErrorType.NOERROR;
		this.correctionSuggested = Correction.GOOD;
		this.userDecision = des;
	}

	/**
	 * Constructor Type 2
	 *
	 * @param startPosition
	 * @param length
	 * @param uri
	 */

	public NamedEntityCorrections(int startPosition, int length, String uri) {
		super(startPosition, length, uri);
		// TODO Auto-generated constructor stub
		this.error = ErrorType.NOERROR;
		this.correctionSuggested = Correction.GOOD;
		this.partner = null;

	}

	/**
	 * Constructor
	 *
	 * @param startPosition
	 * @param length
	 * @param uris
	 * @param d
	 */

	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, Document d) {
		super(startPosition, length, uris);
		// TODO Auto-generated constructor stub
		this.error = ErrorType.NOERROR;
		this.correctionSuggested = Correction.GOOD;
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
	public NamedEntityCorrections(int startPosition, int length, String uri, ErrorType error, Correction result,
			NamedEntityCorrections partner) {
		super(startPosition, length, uri);
		this.error = error;
		this.correctionSuggested = result;
		this.partner = partner;

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
	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, ErrorType error,
			Correction correction) {
		// TODO Auto-generated constructor stub
		super(startPosition, length, uris);
		this.error = error;
		this.correctionSuggested = correction;

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

	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, ErrorType error, Correction result,
			DecisionValue userDecision) {
		super(startPosition, length, uris);
		this.correctionSuggested = result;
		this.error = error;
		this.userDecision = userDecision;
	}

	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, ErrorType error,
			DecisionValue userDecision) {
		super(startPosition, length, uris);
		this.correctionSuggested = Correction.NOTDEFINED;
		this.error = error;
		this.userDecision = userDecision;
	}

	public NamedEntityCorrections(int startPosition, int length, Set<String> uris, Correction insert) {
		super(startPosition, length, uris);
		this.correctionSuggested = insert;
		this.error = ErrorType.NOERROR;
	}

	/**
	 * @return the correctionSuggested
	 */
	public Correction getCorrectionSuggested() {
		return correctionSuggested;
	}

	/**
	 * @param correctionSuggested
	 *            the correctionSuggested to set
	 */
	public void setCorrectionSuggested(Correction correctionSuggested) {
		this.correctionSuggested = correctionSuggested;
	}

	/**
	 * @return the partner
	 */
	public NamedEntityCorrections getPartner() {
		return partner;
	}

	/**
	 * @param partner
	 *            the partner to set
	 */
	public void setPartner(NamedEntityCorrections partner) {
		this.partner = partner;
	}

	/**
	 * @return the entity_name
	 */
	public String getEntity_name() {
		return entity_name;
	}

	/**
	 * @param entity_name
	 *            the entity_name to set
	 */
	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	/**
	 * @return the doc
	 */
	public String getDoc() {
		return doc;
	}

	/**
	 * @param doc
	 *            the doc to set
	 */
	public void setDoc(String doc) {
		this.doc = doc;
	}

	/**
	 * @return the entity_text
	 */
	public String[] getEntity_text() {
		return entity_text;
	}

	/**
	 * @param entity_text
	 *            the entity_text to set
	 */
	public void setEntity_text(String[] entity_text) {
		this.entity_text = entity_text;
	}

	/**
	 * @return the number_of_lemma
	 */
	public int getNumber_of_lemma() {
		return number_of_lemma;
	}

	/**
	 * @param number_of_lemma
	 *            the number_of_lemma to set
	 */
	public void setNumber_of_lemma(int number_of_lemma) {
		this.number_of_lemma = number_of_lemma;
	}

	/**
	 * @return the error
	 */
	public ErrorType getError() {
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(ErrorType error) {
		this.error = error;
	}

	/**
	 * @return the userDecision
	 */
	public DecisionValue getUserDecision() {
		return userDecision;
	}

	/**
	 * @param userDecision
	 *            the userDecision to set
	 */
	public void setUserDecision(DecisionValue userDecision) {
		this.userDecision = userDecision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((partner == null) ? 0 : partner.hashCode());
		result = prime * result + ((this.correctionSuggested == null) ? 0 : this.correctionSuggested.hashCode());
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
		if (correctionSuggested != other.correctionSuggested)
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
		builder.append(correctionSuggested);
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
