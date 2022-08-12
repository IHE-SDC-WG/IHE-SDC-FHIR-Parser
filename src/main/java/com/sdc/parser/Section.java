package com.sdc.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

public class Section {

	protected String sectionName;
	protected String sectionTitle;
	protected String sectionId;
	protected List<Section> subSections = new ArrayList<>();
	protected List<Question> questions = new ArrayList<>();
	private Element sectionElement; // the XML of the form. A section element contains a sectionProperties element
									// and a childItems element
	protected Element sectionProperties;
	// protected Element childItems;

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

	// public Section(String sectionName, Element sectionProperties, Element childItems) {
	// 	this(sectionName, sectionProperties);
	// 	this.setChildItems(childItems);
	// }

	/* should set the sectionElement and parse the element into a Section */
	public Section(Element sectionElement) {
		this.sectionElement = sectionElement;
		this.sectionId = sectionElement.getAttribute("ID");
		this.sectionTitle = sectionElement.getAttribute("Title");

		List<Element> childElems = ParserHelper.nodeListToElemArray(sectionElement.getElementsByTagName("ChildItems").item(0).getChildNodes());
		this.subSections = childElems.stream().filter(ParserHelper.isSection()).map(elem -> new Section(elem)).toList();
		this.questions = childElems.stream().filter(ParserHelper.isQuestion()).map(elem -> new Question(elem)).toList();
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

	// public Element getChildItems() {
	// 	return this.childItems;
	// }

	// public void setChildItems(Element childItems) {
	// 	this.childItems = childItems;
	// }

	public Element getSectionElement() {
		return this.sectionElement;
	}
}
