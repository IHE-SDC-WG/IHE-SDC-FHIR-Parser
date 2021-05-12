package com.sdc.parser.Resource;

import static com.sdc.parser.Constants.Constants.BOOLEAN;
import static com.sdc.parser.Constants.Constants.DATETIME;
import static com.sdc.parser.Constants.Constants.DECIMAL;
import static com.sdc.parser.Constants.Constants.INTEGER;
import static com.sdc.parser.Constants.Constants.STRING;
import static com.sdc.parser.Constants.Constants.SYSTEM_NAME;
import static com.sdc.parser.ParserHelper.getTextResponseForType;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.w3c.dom.Element;

import ca.uhn.fhir.context.FhirContext;

public class ObservationHelper {
	
	public static Observation buildObservationResource(Element questionElement, Element listItemElement, String id,
			FhirContext ctx) {

		Observation observation = new Observation();
		// observation.setSubject(new Reference("Patient/6754"));
		observation.addIdentifier().setSystem(SYSTEM_NAME).setValue(id + "#" + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem(SYSTEM_NAME).setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		observation.setValue(new CodeableConcept()).getValueCodeableConcept().addCoding().setSystem(SYSTEM_NAME)
				.setCode(listItemElement.getAttribute("ID")).setDisplay(listItemElement.getAttribute("title"));
		// observation.addDerivedFrom().setReference("DocumentReference/" + id);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
		return observation;

	}

	public static Observation buildMultiSelectObservationResource(Element questionElement, String id, FhirContext ctx) {
		Observation observation = new Observation();
		// observation.setSubject(new Reference("Patient/6754"));
		observation.addIdentifier().setSystem(SYSTEM_NAME).setValue(id + "." + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem(SYSTEM_NAME).setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		// observation.addDerivedFrom().setReference("DocumentReference/" + id);
		return observation;
	}

	public static void buildAndAddObservationForType(String type, Element textQuestionResponse, Element questionElement,
			String Id, FhirContext ctx, ArrayList<Observation> observations) {
		String response = getTextResponseForType(type, textQuestionResponse);
		Observation observation = buildTextObservationResource(type, questionElement, response, Id, ctx);
		observations.add(observation);
	}

	public static Observation buildTextObservationResource(String type, Element questionElement, String textResponse,
			String id, FhirContext ctx) {
		try {
			Observation observation = new Observation();
			observation.setSubject(new Reference("Patient/6547"));
			observation.addPerformer().setReference("Practitioner/pathpract1");
			observation.addIdentifier().setSystem(SYSTEM_NAME).setValue(id + "#" + questionElement.getAttribute("ID"));
			observation.setStatus(ObservationStatus.FINAL);
			observation.getCode().addCoding().setSystem(SYSTEM_NAME).setCode(questionElement.getAttribute("ID"))
					.setDisplay(questionElement.getAttribute("title"));
			if (type == INTEGER) {
				observation.setValue(new IntegerType(Integer.parseInt(textResponse))).getValueIntegerType();
			} else if (type == DECIMAL) {
				observation.setValue(new Quantity(Double.parseDouble(textResponse))).getValueQuantity();
			} else if (type == STRING) {
				observation.setValue(new StringType(textResponse)).getValueStringType();
			} else if (type == BOOLEAN) {
				observation.setValue(new BooleanType(Boolean.parseBoolean(textResponse))).getValueBooleanType();
			} else if (type == DATETIME) {
				observation.setValue(new DateTimeType(textResponse)).getValueDateTimeType();
			} else {
				String notSupportedError = "ERROR: BUILDING OBERVATION FOR UNSUPPORTED TYPE";
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST).entity(notSupportedError).build());
			}
//			observation.addDerivedFrom().setReference("DocumentReference/" + id);
//			String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
//			System.out.println(encoded);
//			System.out.println("*******************************************************************");
			return observation;
		} catch (NumberFormatException e) {
			String errorMessage = "Error 400 Bad Request (Number Format Exception): " + e.getMessage()
					+ " is not of the correct type for question with ID " + questionElement.getAttribute("ID");
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XHTML_XML)
					.entity(errorMessage).build());
		}
	}

	public static Observation addComponentToObservation(Observation observation,
			ArrayList<Element> listElementsAnswered) {

		for (Element element : listElementsAnswered) {

			observation.addComponent().getCode().addCoding().setSystem(SYSTEM_NAME).setCode(element.getAttribute("ID"))
					.setDisplay(element.getAttribute("title"));
		}

		return observation;
	}
	
}
