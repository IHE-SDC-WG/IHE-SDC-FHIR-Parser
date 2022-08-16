package com.sdc.parser;

import org.w3c.dom.Element;

public class Question extends ObservationElement { 
	protected boolean mustImplement;
	protected int minCard; // min cardinality of the question

	/*
	 * constructors to create Questions from parsed information (if parse within
	 * this class is not desired)
	 */
	public Question(String title, String ID, String name) {
		super(title, ID, name);
	}

	public Question(String title, String ID, String name, boolean mustImplement) {
		super(title, ID, name);
		this.mustImplement = mustImplement;
	}

	public Question(String title, String ID, String name, boolean mustImplement, int minCard) {
		this(title, ID, name, mustImplement);
		this.minCard = minCard;
	}

	public Question(Element element) {
		super(element);
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
