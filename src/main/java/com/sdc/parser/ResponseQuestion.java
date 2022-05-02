package com.sdc.parser;

import org.w3c.dom.Element;

public class ResponseQuestion extends Question {

	private Element responseField; // TODO turn it into object for nested questions
	private String response;
	private String responseType;

	/*
	 * constructors to create ResponseQuestion from parsed information (if parse
	 * within this class is not desired)
	 */
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

	/*
	 * should set the questionElement and parse the element into a ResponseQuestion
	 */
	public ResponseQuestion(Element questionElement) {
		super(questionElement);

		/* parse list question element */
	}

	/* getters and setters below */
	public Element getResponseField() {
		return this.responseField;
	}

	public String getResponse() {
		return this.response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseType() {
		return this.responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public void setResponseField(Element responseField) {
		this.responseField = responseField;
	}

}
