package com.sdc.parser.Constants;

public final class Constants {
	private Constants() {
		// restrict instantiation
	}

	public static final String LANDING_MESSAGE = "<h1>Welcome to Canada Health Infoway's SDC Parser Service</h1>"
			+ "<p></p><h3> Optional Paremeters: server = [FHIR Server endpoint the resources will be posted to]</h3>"
			+ "<h3>Ex: /sdcparser?server=http://test.fhir.org/r4</h3>"
			+ "<p></p><h3> Optional Paremeters: format = json/xml </h3>"
			+ "<h3>Ex: /sdcparser?server=http://test.fhir.org/r4&format=json</h3>";

	public static final String INTEGER = "integer";
	public static final String DECIMAL = "decimal";
	public static final String STRING = "string";
	public static final String BOOLEAN = "boolean";
	public static final String DATE = "date";
	public static final String DATETIME = "dateTime";

	public static final String PROVENANCE_HEADER = "{\"resourceType\": \"Provenance\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"TIME_STAMP\"},\"recorded\": \"TIME_STAMP\",\"agent\": [{\"type\": {\"text\": \"Joel and Alex testing\"}}]}";

	public static final String SYSTEM_NAME = "https://CAP.org/eCC";
	public static final String PROVENANCE_SYSTEM_NAME = "http://terminology.hl7.org/CodeSystem/provenance-participant-type";
	public static final String EVENT_CODING_SYSTEM_NAME = "http://example.org/fhir/message-events";

	public static final String MESSAGE_HEADER_TEXT = "SDC example Transaction Bundle for fake patient Jose inclouding Patient, Encounter";

	public static final String TEXT_PARSING_ERROR_MSG = "ERROR. TextQuestion type is not accounted for!!!!!";
//	public static final String provenanceHeader = "{\"resourceType\": \"Provenance\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"2020-08-31T20:44:24.994+00:00\"},\"recorded\": \"2020-05-14T13:44:24.1703291-07:00\",\"agent\": [{\"type\": {\"text\": \"Joel and Alex testing\"}}]}";

}
