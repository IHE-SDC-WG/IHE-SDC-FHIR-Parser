package com.sdc.parser.Resource;

import static com.sdc.parser.ParserHelper.getUUID;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

import com.sdc.parser.FormParser;
import com.sdc.parser.Section;
import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.SpecialTypes.ObservationType;
import com.sdc.parser.Config.SpecialTypes.TextResponseType;

import ca.uhn.fhir.context.FhirContext;

public class ObservationHelper {
	public static ArrayList<Observation> buildObservationResources(ObservationType obsType,
			TextResponseType textResponseType,
			Element questionElement, ArrayList<Element> listItemElements, String textResponse, String id,
			FhirContext ctx, ConfigValues configValues) {
		Observation initialObservation = new Observation();
		ArrayList<Observation> builtObservations = new ArrayList<Observation>();
		if (obsType.equals(ObservationType.LIST)) {
		} else if (obsType.equals(ObservationType.MULTISELECT)) {
		} else if (obsType.equals(ObservationType.TEXT)) {
			switch (textResponseType) {
				case INTEGER:
					initialObservation.setValue(new IntegerType(Integer.parseInt(textResponse))).getValueIntegerType();
					break;
				case DECIMAL:
					initialObservation.setValue(new Quantity(Double.parseDouble(textResponse))).getValueQuantity();
					break;
				case STRING:
					initialObservation.setValue(new StringType(textResponse)).getValueStringType();
					break;
				case BOOLEAN:
					initialObservation.setValue(new BooleanType(Boolean.parseBoolean(textResponse)))
							.getValueBooleanType();
					break;
				case DATETIME:
					initialObservation.setValue(new DateTimeType(textResponse)).getValueDateTimeType();
					break;
				default:
					String notSupportedError = "ERROR: BUILDING OBSERVATION FOR UNSUPPORTED TYPE";
					throw new WebApplicationException(
							Response.status(Status.BAD_REQUEST).entity(notSupportedError).build());
			}
		}
		;

		// When the solution to a question is a list, store the listitem response as a
		// codeableconcept
		List<Observation> observations = addListItemsToCodeableConcept(listItemElements, configValues,
				initialObservation, obsType);
		observations.forEach(observation -> {
			addObservationMetadata(questionElement, configValues, observation);
		});
		builtObservations.addAll(observations);

		NodeList subQuestionsList = questionElement.getElementsByTagName("Question");

		// Track the hierarchy of observations with derivedfrom and hasmember
		List<Observation> subAnswers = FormParser.getAnsweredQuestions(subQuestionsList, id, ctx, configValues);
		addRelationHierarchy(initialObservation, subAnswers);
		builtObservations.addAll(subAnswers);
		return builtObservations;
	}

	public static List<Observation> populateRelationHierarchies(List<Section> sectionObjs, List<Observation> sectionObservations) {
		sectionObservations.forEach(sectionObservation -> {
			Section matchingSection = getObservationsSection(sectionObjs, sectionObservation.getCode().getCodingFirstRep().getCode());
			List<Observation> subSectionObservations = matchingSection.getSubSections().stream().map(subSection -> sectionObservations.stream().filter(sec -> {
				boolean isSubSection = sec.getCode().getCodingFirstRep().getCode().equals(subSection.getID());
				return isSubSection;
			}).findFirst().get()).toList();
			ObservationHelper.addRelationHierarchy(sectionObservation, subSectionObservations);
		});
		return sectionObservations;
	}

	private static Section getObservationsSection(List<Section> sectionList, String sectionId) {
		return sectionList.stream().filter(section -> section.getID().equals(sectionId)).findFirst().get();
	}

	public static void addRelationHierarchy(Observation parentObservation, List<Observation> subObservations) {
		if (subObservations.size() > 0) {
			for (Observation subObservation : subObservations) {
				addDerivedAndMemberRelations(parentObservation, subObservation);
			}
		}
	}

	public static void addDerivedAndMemberRelations(Observation derivedResource, Observation memberResource) {
		memberResource
				.addDerivedFrom(new Reference().setIdentifier(derivedResource.getIdentifierFirstRep()));
		derivedResource.addHasMember(new Reference().setIdentifier(memberResource.getIdentifierFirstRep()));
	}

	private static List<Observation> addListItemsToCodeableConcept(ArrayList<Element> listItemElements,
			ConfigValues configValues,
			Observation observation, ObservationType obsType) {
		List<Observation> splitObservations = new ArrayList<>() {
			{
				add(observation);
			}
		};
		if (listItemElements != null) {
			observation.setValue(new CodeableConcept());
			listItemElements.stream()
					.forEach(element -> {
						Observation observationToEdit = observation;
						if (obsType.equals(ObservationType.MULTISELECT)) {
							Observation newObservation = observation.copy();
							observationToEdit = newObservation;
							splitObservations.add(observationToEdit);
						}
						observationToEdit.getValueCodeableConcept().addCoding()
								.setSystem(configValues.getSystemName())
								.setCode(element.getAttribute("ID")).setDisplay(element.getAttribute("title"));
					});

				splitObservations.forEach(obs -> vccTextReplacement(listItemElements, obs));
		}
		return splitObservations;
	}

	private static void vccTextReplacement(ArrayList<Element> listItemElements, Observation observation) {
		String vccText = observation.getValueCodeableConcept().getText();
		if (vccText == null) {
			listItemElements.stream()
					.map(e -> e.getElementsByTagName("string"))
					.filter(stringEl -> stringEl.getLength() > 0)
					.map(stringEl -> ((Element) stringEl.item(0)).getAttribute("val"))
					.filter(listItemString -> listItemString.length() > 0)
					.forEach(listItemString -> observation.getValueCodeableConcept().setText(listItemString));
		}
	}

	public static void addObservationMetadata(Observation observation, String elemId, String elemTitle, String systemName) {
		observation.addIdentifier().setSystem(systemName)
				.setValue(getUUID());
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem(systemName)
				.setCode(elemId)
				.setDisplay(elemTitle);
	}

	private static void addObservationMetadata(Element element, ConfigValues configValues, Observation observation) {
		String elemId = element.getAttribute("ID");
		String elemTitle = element.getAttribute("title");
		addObservationMetadata(configValues, observation, elemId, elemTitle);
	}

	private static void addObservationMetadata(ConfigValues configValues, Observation observation, String elemId, String elemTitle) {
		String systemName = configValues.getSystemName();
		addObservationMetadata(observation, elemId, elemTitle, systemName);
	}
}
