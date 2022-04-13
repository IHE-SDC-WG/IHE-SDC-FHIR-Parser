package com.sdc.parser.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sdc.parser.FormParser;
import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.SpecialTypes.ObservationType;
import com.sdc.parser.Config.SpecialTypes.TextResponseType;

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
import org.w3c.dom.NodeList;

import ca.uhn.fhir.context.FhirContext;

public class ObservationHelper {
	public static ArrayList<Observation> buildObservationResources(ObservationType obsType,
			TextResponseType textResponseType,
			Element questionElement, ArrayList<Element> listItemElements, String textResponse, String id,
			FhirContext ctx, ConfigValues configValues) {
		Observation observation = new Observation();
		ArrayList<Observation> builtObservations = new ArrayList<Observation>();
		String separator = "";

		if (obsType.equals(ObservationType.LIST)) {
			separator = "#";
		} else if (obsType.equals(ObservationType.MULTISELECT)) {
			separator = ".";
		} else if (obsType.equals(ObservationType.TEXT)) {
			separator = "#";
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
		addObservationMetaData(questionElement, id, observation, separator, configValues);
		builtObservations.add(observation);

		// When the solution to a question is a list, store the listitem response as a
		// codeableconcept
		if (listItemElements != null) {
			observation.setValue(new CodeableConcept());
			String vccText = observation.getValueCodeableConcept().getText();

			listItemElements.stream()
					.forEach(element -> {
						observation.getValueCodeableConcept().addCoding()
								.setSystem(configValues.getSystemName())
								.setCode(element.getAttribute("ID")).setDisplay(element.getAttribute("title"));
					});

			if (vccText == null) {
				listItemElements.stream()
						.map(e -> e.getElementsByTagName("string"))
						.filter(stringEl -> stringEl.getLength() > 0)
						.map(stringEl -> ((Element) stringEl.item(0)).getAttribute("val"))
						.filter(listItemString -> listItemString.length() > 0)
						.forEach(listItemString -> observation.getValueCodeableConcept().setText(listItemString));
			}
		}

		NodeList subQuestionsList = questionElement.getElementsByTagName("Question");

		// Track the hierarchy of observations with derivedfrom and hasmember
		List<Observation> subAnswers = FormParser.getAnsweredQuestions(subQuestionsList, id, ctx, configValues);
		if (subAnswers.size() > 0) {
			for (Observation subObservation : subAnswers) {
				subObservation
						.addDerivedFrom(new Reference().setIdentifier(observation.getIdentifierFirstRep()));
				observation.addHasMember(new Reference().setIdentifier(subObservation.getIdentifierFirstRep()));
			}
			builtObservations.addAll(subAnswers);
		}
		return builtObservations;
	}

	private static void addObservationMetaData(Element element, String id, Observation observation, String separator,
			ConfigValues configValues) {
		observation.addIdentifier().setSystem(configValues.getSystemName())
				.setValue(id + separator + element.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem(configValues.getSystemName()).setCode(element.getAttribute("ID"))
				.setDisplay(element.getAttribute("title"));
	}

}
