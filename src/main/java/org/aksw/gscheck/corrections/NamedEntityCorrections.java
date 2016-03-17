package org.aksw.gscheck.corrections;

import java.util.ArrayList;
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

	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String entity_text) {
		this.doc = entity_text;
	}

	public NamedEntityCorrections(int startPosition, int length, String string) {
		super(startPosition, length, string);
		// TODO Auto-generated constructor stub
		result = Check.GOOD;
		partner = null;

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

}
