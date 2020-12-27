package com.sdc.parser;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class Section {

	protected String sectionName;
	protected List<Section> subSections;
	protected List<Question> questions;
	private Element sectionElement; // the XML of the form. A section element contains a sectionProperties element
									// and a childItems element
	protected Element sectionProperties;
	protected Element childItems;

	/* base constructor to initialize the Lists */
	public Section() {
		this.initSubSections();
	}

	/*
	 * constructors to create Sections from parsed information (if parse within this
	 * class is not desired)
	 */
	public Section(String sectionName) {
		this();
		this.setSectionName(sectionName);
	}

	public Section(String sectionName, Element sectionProperties) {
		this(sectionName);
		this.setSectionProperties(sectionProperties);
	}

	public Section(String sectionName, Element sectionProperties, Element childItems) {
		this(sectionName, sectionProperties);
		this.setChildItems(childItems);
	}

	/* should set the sectionElement and parse the element into a Section */
	public Section(Element sectionElement) {
		this();
		this.sectionElement = sectionElement;

		/* Parse section here */
	}

	/* Initializers for the Lists below */
	protected void initSubSections() {
		this.subSections = new ArrayList<Section>();
	}

	/* getters and setters below */
	public String getSectionName() {
		return this.sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public List<Section> getSubSections() {
		return this.subSections;
	}

	public void addSubSection(Section subSection) {
		this.subSections.add(subSection);
	}

	public void addSubSections(List<Section> subSections) {
		this.subSections.addAll(subSections);
	}

	public List<Question> getQuestions() {
		return this.questions;
	}

	public void addQuestions(Question question) {
		this.questions.add(question);
	}

	public void addQuestions(List<Question> questions) {
		this.questions.addAll(questions);
	}

	public Element getSectionProperties() {
		return this.sectionProperties;
	}

	public void setSectionProperties(Element sectionProperties) {
		this.sectionProperties = sectionProperties;
	}

	public Element getChildItems() {
		return this.childItems;
	}

	public void setChildItems(Element childItems) {
		this.childItems = childItems;
	}

	public Element getSectionElement() {
		return this.sectionElement;
	}
}
