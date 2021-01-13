package com.sdc.parser;

import org.w3c.dom.Element;

public class DisplayedItem {

	/*
	 * sample: <DisplayedItem name="DI_25893" order="16" ID="25893.100004300"
	 * title="title"> <Property name="p_rptTxt_25893_1" order="17"
	 * propName="reportText" val="{no text}" /> </DisplayedItem>
	 */

	protected String name;
	protected String ID;
	protected String title;
	protected Element displayedItemElement;

	public DisplayedItem(Element displayedItemElement) {
		this.displayedItemElement = displayedItemElement;
		/* parse displayed item */
	}

	public DisplayedItem(String name, String ID, String title) {
		this.name = name;
		this.ID = ID;
		this.title = title;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getID() {
		return this.ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
