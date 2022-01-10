package com.sdc.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;;

public class Form {

	private Document formDocument; // the XML of the form
	protected List<Element> formProperties; // the properties elements of the form

	// Attributes within FromDesign element
	protected String title;
	protected String ID;
	protected String version;
	protected String lineage;
	protected String baseURI;
	protected String fullURI;
	protected String fileName;
	protected List<Section> formSections; // sections in the form
	protected List<Question> formQuestions; // form can have questions outside of sections
	protected List<DisplayedItem> formDisplayedItems; // display items of form

	/* base constructor to initialize the Lists */
	public Form() {
		this.initFormProperties();
		this.initFormQuestions();
		this.initFormDisplayedItems();
	}

	/*
	 * constructors to create Forms from parsed information (if parse within this
	 * class is not desired)
	 */
	public Form(String title) {
		this();
		this.setTitle(title);
	}

	public Form(String title, String ID) {
		this(title);
		this.setID(ID);
	}

	public Form(String title, String ID, String version) {
		this(title, ID);
		this.setVersion(version);
	}

	public Form(String title, String ID, String version, List<Element> formProperties) {
		this(title, ID, version);
		this.addFormProperties(formProperties);
	}

	/* should set the formDocument and parse the document into a From */
	public Form(Document document) {
		this();
		this.setFormDocument(document);

		/* parse form here */
		/* look at FormParser.parseSDCForm for parsing */
	}

	/* Initializers for the Lists below */
	protected void initFormProperties() {
		this.formProperties = new ArrayList<Element>();
	}

	protected void initFormQuestions() {
		this.formQuestions = new ArrayList<Question>();
	}

	protected void initFormDisplayedItems() {
		this.formDisplayedItems = new ArrayList<DisplayedItem>();
	}

	/* getters and setters below */
	public List<Element> getFormProperties() {
		return this.formProperties;
	}

	public void addFormProperty(Element formProperty) {
		this.formProperties.add(formProperty);
	}

	public void addFormProperties(List<Element> formProperties) {
		this.formProperties.addAll(formProperties);
	}

	public List<Section> getFormSections() {
		return this.formSections;
	}

	public void addFormSection(Section formSection) {
		this.formSections.add(formSection);
	}

	public void addFormSections(List<Section> formSections) {
		this.formSections.addAll(formSections);
	}

	public List<Question> getFormQuestions() {
		return this.formQuestions;
	}

	public void addFormQuestion(Question formQuestion) {
		this.formQuestions.add(formQuestion);
	}

	public void addFormQuestions(List<Question> formQuestions) {
		this.formQuestions.addAll(formQuestions);
	}

	public List<DisplayedItem> getFormDisplayedItems() {
		return this.formDisplayedItems;
	}

	public void addFormDisplayedItem(DisplayedItem formDisplayedItem) {
		this.formDisplayedItems.add(formDisplayedItem);
	}

	public void addFormSDisplayedItems(List<DisplayedItem> formDisplayedItems) {
		this.formDisplayedItems.addAll(formDisplayedItems);
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

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLineage() {
		return this.lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public String getBaseURI() {
		return this.baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getFullURI() {
		return this.fullURI;
	}

	public void setFullURI(String fullURI) {
		this.fullURI = fullURI;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Document getFormDocument() {
		return this.formDocument;
	}

	private void setFormDocument(Document formDocument) {
		this.formDocument = formDocument;
	}

}
