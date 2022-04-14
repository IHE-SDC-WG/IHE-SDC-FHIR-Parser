package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.context.FhirContext;

public class MessageHeaderHelper extends ResourceHelper<MessageHeader> {

	private static final String SOURCE_ENDPOINT = "http://localhost:8080/sdcparser";
	private static final String SOURCE_NAME = "IHE SDC on FHIR Parser";
	private static final String VALUE_CODE = "cancer-report-message";
	private static final String EVENT_CODING_SYSTEM_NAME = "http://example.org/fhir/message-events";
	private static final String PROFILE_URL = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-message-header";
	private static final String VALUESET_URL = "http://hl7.org/fhir/us/medmorph/2021Jan/CodeSystem-us-ph-messageheader-message-types.html";

	public MessageHeader initializeResource(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.getText().setStatus(NarrativeStatus.GENERATED);
		messageHeader.getEventCoding().setSystem(EVENT_CODING_SYSTEM_NAME).setCode("admin-notify");
		messageHeader.getSource().setName(SOURCE_NAME).setEndpoint(SOURCE_ENDPOINT);
		messageHeader.getMeta().addProfile(PROFILE_URL);
		messageHeader.addExtension(VALUESET_URL, (Type) new CodeType(VALUE_CODE));
		return messageHeader;
	}
}