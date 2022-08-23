package com.sdc.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// import java.util.AbstractMap.SimpleEntry;

import org.hl7.fhir.r4.model.Observation;
import org.w3c.dom.Element;

import com.sdc.parser.Resource.ObservationHelper;

public class Section extends ObservationElement {
	protected List<Section> subSections = new ArrayList<>();
	protected List<Question> questions = new ArrayList<>();
	protected Element sectionProperties;
	// protected Element childItems;

	/* base constructor to initialize the Lists */
	public Section() throws IOException {
		this.initSubSections();
	}

	/*
	 * constructors to create Sections from parsed information (if parse within this
	 * class is not desired)
	 */
	public Section(String sectionName) throws IOException {
		this();
		this.setName(sectionName);
	}

	public Section(String sectionName, Element sectionProperties) throws IOException {
		this(sectionName);
		this.setSectionProperties(sectionProperties);
	}


	public Section(Element sectionElement, Section parent) throws IOException {
		this.element = sectionElement;
		this.ID = sectionElement.getAttribute("ID");
		this.title = sectionElement.getAttribute("title");

		List<Element> childElems = ParserHelper.nodeListToElemArray(sectionElement.getElementsByTagName("ChildItems").item(0).getChildNodes());
		this.subSections = childElems.stream().filter(ParserHelper.isSection()).map(elem -> {
			try {
				return new Section(elem, parent);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).toList();
		this.questions = childElems.stream().filter(ParserHelper.isQuestion()).map(elem -> new Question(elem)).toList();
	}


	/* should set the sectionElement and parse the element into a Section */
	public Section(Element sectionElement) throws IOException {
		this(sectionElement, null);
	}

	public Observation toObservation() {
		return this.toObservation(this.systemName);
	}

	
	public Observation toObservation(String systemName) {
		Observation observation = new Observation();
		ObservationHelper.addObservationMetadata(observation, this.getID(), this.getTitle(), systemName);;
		return observation;
	}
/**
 * returns this section and all subsections in a flattened List
 */
	protected List<Section> flatten() {
		List<Section> flatList = new ArrayList<>();
		flatList.add(this);
		this.subSections.forEach(sub -> flatList.addAll(sub.flatten()));
		return flatList;
	}

	protected void initSubSections() {
		this.subSections = new ArrayList<Section>();
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
}
