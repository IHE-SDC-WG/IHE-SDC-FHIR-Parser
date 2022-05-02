package com.sdc.parser.Config;

public final class SpecialTypes {
	private SpecialTypes() {
		// restrict instantiation
	}

	public static enum TextResponseType {
		INTEGER,
		DECIMAL,
		STRING,
		BOOLEAN,
		DATE,
		DATETIME
	}

	public static enum ObservationType {
		LIST,
		MULTISELECT,
		TEXT
	}

}
