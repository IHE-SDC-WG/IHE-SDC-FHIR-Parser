package com.sdc.parser.Resource;

import java.util.ArrayList;
import java.util.Arrays;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;

import ca.uhn.fhir.context.FhirContext;

public class MessageHeaderHelper {

	private static final String EVENT_CODING_SYSTEM_NAME = "http://example.org/fhir/message-events";
	private static String profileCanonical = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-message-header";

	public static MessageHeader createMessageHeader(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.getText().setStatus(NarrativeStatus.GENERATED);

		messageHeader.getEventCoding().setSystem(EVENT_CODING_SYSTEM_NAME).setCode("admin-notify");
		messageHeader.setSource(new MessageSourceComponent().setName("IHE SDC on FHIR Parser")
				.setEndpoint("http://localhost:8080/sdcparser"));
		messageHeader.setMeta(new Meta()
				.setProfile(new ArrayList<CanonicalType>(Arrays.asList(
						new CanonicalType(profileCanonical)))));
		return messageHeader;
	}
}
