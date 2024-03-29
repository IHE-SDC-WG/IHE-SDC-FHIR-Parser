package com.sdc.parser;

import java.io.IOException;

import org.w3c.dom.Element;

import com.sdc.parser.Config.ConfigValues;

class ObservationElement {
    String ID;
    String title;
    String name;
    Element element;
    String systemName;

    /*
     * constructors to create Questions from parsed information (if parse within this class is not desired)
     */

    public ObservationElement() throws IOException {
        this.systemName = (new ConfigValues()).getSystemName();
    }

    public ObservationElement(String title, String ID, String name) {
        super();
		this.title = title;
		this.ID = ID;
		this.name = name;
	}

    public ObservationElement(Element element) {
        super();
		this.setElement(element);
		this.setID(this.getElement().getAttribute("ID"));

		/* parse question element */
	}

    public String getID() {
        return this.ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    private void setElement(Element element) {
        this.element = element;
    }

    /* getters and setters below */
    public Element getElement() {
        return this.element;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}