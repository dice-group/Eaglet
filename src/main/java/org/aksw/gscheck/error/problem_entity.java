package org.aksw.gscheck.error;

public class problem_entity {
	String doc;
	int start_pos;
	int length;
	String entity_name;
	String problem_text;
	String entity_text;
	int end_pos;

	public int getEnd_pos() {
		return end_pos;
	}

	public void setEnd_pos(int end_pos) {
		this.end_pos = end_pos;
	}

	public String getEntity_text() {
		return entity_text;
	}

	public void setEntity_text(String entity_text) {
		this.entity_text = entity_text;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public int getStart_pos() {
		return start_pos;
	}

	public void setStart_pos(int start_pos) {
		this.start_pos = start_pos;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity) {
		this.entity_name = entity;
	}

	public String getProblem_text() {
		return problem_text;
	}

	public void setProblem_text(String problem_text) {
		this.problem_text = problem_text;
	}

}
