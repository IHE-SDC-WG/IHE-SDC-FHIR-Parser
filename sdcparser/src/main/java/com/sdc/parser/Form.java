package com.sdc.parser;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class Form {

	protected List<Element> formProperties;
	protected String title;
	protected String ID;
	protected String version;
	protected String lineage;
	protected String baseURI;
	protected String fullURI;
	protected String fileName;

	public Form() {
		this.initFormProperties();
	}

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

	protected void initFormProperties() {
		this.formProperties = new ArrayList<Element>();
	}

	public List<Element> getFormProperties() {
		return this.formProperties;
	}

	public void addFormProperty(Element formProperty) {
		this.formProperties.add(formProperty);
	}

	public void addFormProperties(List<Element> formProperties) {
		this.formProperties.addAll(formProperties);
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

}
