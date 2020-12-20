package com.sdc.parser;

public class Question {
	protected String title;
	protected String ID;
	protected String name;
	protected boolean mustImplement;
	protected int minCard;

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

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getID() {
		return this.ID;
	}

	public void setID(String iD) {
		this.ID = iD;
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
