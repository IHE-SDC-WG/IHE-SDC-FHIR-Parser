package com.sdc.parser.Resource;

import static com.sdc.parser.Constants.Constants.SYSTEM_NAME;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sdc.parser.Constants.Constants.ObservationType;
import com.sdc.parser.Constants.Constants.TextResponseType;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.w3c.dom.Element;

import ca.uhn.fhir.context.FhirContext;

public class ObservationHelper {
	public static ArrayList<Observation> buildObservationResources(ObservationType obsType,
			TextResponseType textResponseType,
			Element questionElement, ArrayList<Element> listItemElements, String textResponse, String id,
			FhirContext ctx) {
		Observation observation = new Observation();
		ArrayList<Observation> builtObservations = new ArrayList<Observation>();
		String separator = "";
		if (obsType.equals(ObservationType.LIST)) {
			separator = "#";
		} else if (obsType.equals(ObservationType.MULTISELECT)) {
			separator = ".";
		} else if (obsType.equals(ObservationType.TEXT)) {
			separator = "#";
			// TODO: Does patient and practitioner need to be in each observation?
			// observation.setSubject(new Reference("Patient/6754"));
			// observation.addPerformer().setReference("Practitioner/pathpract1");
			switch (textResponseType) {
				case INTEGER:
					observation.setValue(new IntegerType(Integer.parseInt(textResponse))).getValueIntegerType();
					break;
				case DECIMAL:
					observation.setValue(new Quantity(Double.parseDouble(textResponse))).getValueQuantity();
					break;
				case STRING:
					observation.setValue(new StringType(textResponse)).getValueStringType();
					break;
				case BOOLEAN:
					observation.setValue(new BooleanType(Boolean.parseBoolean(textResponse))).getValueBooleanType();
					break;
				case DATETIME:
					observation.setValue(new DateTimeType(textResponse)).getValueDateTimeType();
					break;
				default:
					String notSupportedError = "ERROR: BUILDING OBSERVATION FOR UNSUPPORTED TYPE";
					throw new WebApplicationException(
							Response.status(Status.BAD_REQUEST).entity(notSupportedError).build());
			}
		}
		addObservationMetaData(questionElement, id, observation, separator);
		builtObservations.add(observation);

		if (listItemElements != null) {
			for (Element element : listItemElements) {
				observation.setValue(new CodeableConcept()).getValueCodeableConcept().addCoding().setSystem(SYSTEM_NAME)
						.setCode(element.getAttribute("ID")).setDisplay(element.getAttribute("title"));
				Observation listItemObservation = buildListItemObservation(observation, element, id, separator);
				observation.addHasMember(new Reference().setIdentifier(listItemObservation.getIdentifierFirstRep()));
				builtObservations.add(listItemObservation);
			}
		}
		return builtObservations;
	}

	private static void addObservationMetaData(Element element, String id, Observation observation, String separator) {
		observation.addIdentifier().setSystem(SYSTEM_NAME)
				.setValue(id + separator + element.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem(SYSTEM_NAME).setCode(element.getAttribute("ID"))
				.setDisplay(element.getAttribute("title"));
	}

	private static Observation buildListItemObservation(Observation derivedObservation, Element listItemElement,
			String id, String separator) {
		Observation listItemObservation = new Observation();
		addObservationMetaData(listItemElement, id, listItemObservation, separator);
		listItemObservation.addDerivedFrom(new Reference().setIdentifier(derivedObservation.getIdentifierFirstRep()));
		//TODO: add potential answer "value"
		return listItemObservation;
	}
}
