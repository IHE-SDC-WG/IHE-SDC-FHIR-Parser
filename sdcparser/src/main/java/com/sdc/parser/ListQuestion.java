package com.sdc.parser;

import org.w3c.dom.Element;

public class ListQuestion extends Question {
	
	private Element listField; // TODO turn it into object for nested questions

	public ListQuestion(String title, String ID, String name, Element listField) {
		super(title, ID, name);
		// TODO Auto-generated constructor stub
		this.setListField(listField);
	}

	public ListQuestion(String title, String ID, String name, Element listField, boolean mustImplement) {
		super(title, ID, name, mustImplement);
		// TODO Auto-generated constructor stub
		this.setListField(listField);
	}

	public ListQuestion(String title, String ID, String name, Element listField, boolean mustImplement, int minCard) {
		super(title, ID, name, mustImplement, minCard);
		// TODO Auto-generated constructor stub
		this.setListField(listField);
	}
	
	public Element getListField() {
		return this.listField;
	}

	public void setListField(Element listField) {
		this.listField = listField;
	}


}
