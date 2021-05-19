package com.sdc.parser.Resource;

import static com.sdc.parser.Constants.Constants.EVENT_CODING_SYSTEM_NAME;
import static com.sdc.parser.Constants.Constants.MESSAGE_HEADER_TEXT;

import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import ca.uhn.fhir.context.FhirContext;

public class MessageHeaderHelper {
	public static MessageHeader createMessageHeader(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		//messageHeader.getText().setStatus(NarrativeStatus.GENERATED);
		//messageHeader.getText().setDivAsString("<div>" + MESSAGE_HEADER_TEXT + "</div>");
		/*
		 * Narrative messageHeaderNarrative = new Narrative();
		 * messageHeaderNarrative.setStatus(NarrativeStatus.GENERATED); XhtmlNode
		 * messageHeaderText = new XhtmlNode(NodeType.Element, "p");
		 * messageHeaderText.addText(MESSAGE_HEADER_TEXT);
		 * messageHeaderNarrative.setDiv(messageHeaderText);
		 * messageHeader.setText(messageHeaderNarrative);
		 */
		
		//TODO Update event coding system from MedMorph https://build.fhir.org/ig/HL7/fhir-medmorph/CodeSystem-us-ph-messageheader-message-types.html
		//TODO set code to cancer-report-message
		messageHeader.getEventCoding().setSystem(EVENT_CODING_SYSTEM_NAME).setCode("cancer-report-message");
		messageHeader.setSource(new MessageSourceComponent().setName("IHE SDC on FHIR Parser")
				.setEndpoint("http://localhost:8080/sdcparser"));
		messageHeader.getSender().setDisplay("IHE SDC Parser").setReference("Organization/IHESDCParser0:");
		messageHeader.getReason().addCoding().setCode("new-labresult").setSystem("http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-triggerdefinition-namedevents"); 
		//TODO add an endpoint if possible. 
		messageHeader.addDestination().getReceiver().setReference("Organization/08686").setDisplay("Rosewood Health");
		//Made this focus reference the DiagnosticReport Identifier as reference
		messageHeader.addFocus().setReference("DiagnosticReport/DiagRepIHESDC0");
		//add a focus and fix reason 
		//add Destination, could set the destination based on the the selected destination in the reference implementation. Would need some way of passing that parameter
		//set sender as hard coded Organization
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(messageHeader);
		return messageHeader;
	}
}
