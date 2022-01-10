package com.sdc.parser.Resource;

import static com.sdc.parser.Constants.Constants.EVENT_CODING_SYSTEM_NAME;

import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;

import ca.uhn.fhir.context.FhirContext;

public class MessageHeaderHelper {
	public static MessageHeader createMessageHeader(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.getText().setStatus(NarrativeStatus.GENERATED);
		//messageHeader.getText().setDivAsString("<div>" + MESSAGE_HEADER_TEXT + "</div>");
		/*
		 * Narrative messageHeaderNarrative = new Narrative();
		 * messageHeaderNarrative.setStatus(NarrativeStatus.GENERATED); XhtmlNode
		 * messageHeaderText = new XhtmlNode(NodeType.Element, "p");
		 * messageHeaderText.addText(MESSAGE_HEADER_TEXT);
		 * messageHeaderNarrative.setDiv(messageHeaderText);
		 * messageHeader.setText(messageHeaderNarrative);
		 */
		messageHeader.getEventCoding().setSystem(EVENT_CODING_SYSTEM_NAME).setCode("admin-notify");
		messageHeader.setSource(new MessageSourceComponent().setName("IHE SDC on FHIR Parser")
				.setEndpoint("http://localhost:8080/sdcparser"));
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(messageHeader);
		return messageHeader;
	}
}
