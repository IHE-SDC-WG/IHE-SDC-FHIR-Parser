package com.sdc.parser;

import org.w3c.dom.Element;

public class ResponseQuestion extends Question {

	private Element responseField; // TODO turn it into object for nested questions

	public ResponseQuestion(String title, String ID, String name, Element responseField) {
		super(title, ID, name);
		// TODO Auto-generated constructor stub
		this.setResponseField(responseField);
	}

	public ResponseQuestion(String title, String ID, String name, Element responseField, boolean mustImplement) {
		super(title, ID, name, mustImplement);
		// TODO Auto-generated constructor stub
		this.setResponseField(responseField);
	}

	public ResponseQuestion(String title, String ID, String name, Element responseField, boolean mustImplement,
			int minCard) {
		super(title, ID, name, mustImplement, minCard);
		// TODO Auto-generated constructor stub
		this.setResponseField(responseField);
	}

	public Element getResponseField() {
		return this.responseField;
	}

	public void setResponseField(Element responseField) {
		this.responseField = responseField;
	}

}
