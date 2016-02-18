package org.aksw.gscheck.corrections;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class NamedEntityCorrections extends NamedEntity {
	public enum Check {
		INSERTED, DELETED, GOOD
	}
	Check result;
	public NamedEntityCorrections(int startPosition, int length, Set<String> uris) {
		super(startPosition, length, uris);
		// TODO Auto-generated constructor stub
		result=Check.GOOD;
		
	}
	public Check getResult() {
		return result;
	}
	public void setResult(Check result) {
		this.result = result;
	}
	
	

}
