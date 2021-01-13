package com.sdc.parser;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ListQuestion extends Question {

	private Element listField; // TODO turn it into object for nested questions
	private boolean isMultiSelect;

	// TODO: Create a structure for the listItems
	NodeList listItemList;
	List<Element> listItemElement; // list of ListItem elements. Same as listItemList

	/*
	 * constructors to create ListQuestion from parsed information (if parse within
	 * this class is not desired)
	 */
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

	/* should set the questionElement and parse the element into a ListQuestion */
	public ListQuestion(Element questionElement) {
		super(questionElement);

		/* parse list question element */
	}

	/* getters and setters below */
	public Element getListField() {
		return this.listField;
	}

	public void setListField(Element listField) {
		this.listField = listField;
	}

	public boolean isMultiSelect() {
		return this.isMultiSelect;
	}

	public void setMultiSelect(boolean isMultiSelect) {
		this.isMultiSelect = isMultiSelect;
	}

}
