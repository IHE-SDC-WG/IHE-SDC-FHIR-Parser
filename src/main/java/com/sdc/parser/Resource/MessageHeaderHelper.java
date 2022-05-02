package com.sdc.parser.Resource;

import java.util.ArrayList;
import java.util.Arrays;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.context.FhirContext;

public class MessageHeaderHelper extends ResourceHelper<MessageHeader> {

	private static final String VALUE_CODE = "cancer-report-message";
	private static final String EVENT_CODING_SYSTEM_NAME = "http://example.org/fhir/message-events";
	private static final String PROFILE_URL = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-message-header";
	private static final String VALUESET_URL = "http://hl7.org/fhir/us/medmorph/2021Jan/CodeSystem-us-ph-messageheader-message-types.html";

	public MessageHeader initializeResource(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.getText().setStatus(NarrativeStatus.GENERATED);
		messageHeader.getEventCoding().setSystem(EVENT_CODING_SYSTEM_NAME).setCode("admin-notify");
		messageHeader.setSource(
			new MessageSourceComponent()
				.setName("IHE SDC on FHIR Parser")
				.setEndpoint("http://localhost:8080/sdcparser"));
		messageHeader.setMeta(
			new Meta()
				.setProfile(new ArrayList<CanonicalType>(Arrays.asList(
						new CanonicalType(PROFILE_URL)))));
		messageHeader.setExtension(
			new ArrayList<Extension>(Arrays.asList(
				new Extension(new UriType(VALUESET_URL))
					.setValue((Type) new CodeType(VALUE_CODE))
			))
		);
		return messageHeader;
	}
}
