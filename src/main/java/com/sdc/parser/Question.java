package com.sdc.parser;

import org.w3c.dom.Element;

public class Question {
	protected String title;
	protected String ID;
	protected String name;
	protected boolean mustImplement;
	protected int minCard; // min cardinality of the question
	protected Element questionElement; // the XML of the question

	/*
	 * constructors to create Questions from parsed information (if parse within
	 * this class is not desired)
	 */
	public Question(String title, String ID, String name) {
		this.title = title;
		this.ID = ID;
		this.name = name;
	}

	public Question(String title, String ID, String name, boolean mustImplement) {
		this(title, ID, name);
		this.mustImplement = mustImplement;
	}

	public Question(String title, String ID, String name, boolean mustImplement, int minCard) {
		this(title, ID, name, mustImplement);
		this.minCard = minCard;
	}

	/* should set the questionElement and parse the element into a Question */
	public Question(Element questionElement) {
		this.setQuestionElement(questionElement);

		/* parse question element */
	}

	/* getters and setters below */
	public Element getQuestionElement() {
		return this.questionElement;
	}

	private void setQuestionElement(Element questionElement) {
		this.questionElement = questionElement;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getID() {
		return this.ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMustImplement() {
		return this.mustImplement;
	}

	public void setMustImplement(boolean mustImplement) {
		this.mustImplement = mustImplement;
	}

	public int getMinCard() {
		return this.minCard;
	}

	public void setMinCard(int minCard) {
		this.minCard = minCard;
	}
}
