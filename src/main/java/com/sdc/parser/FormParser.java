package com.sdc.parser;

import static com.sdc.parser.ParserHelper.*;
import static com.sdc.parser.Resource.ObservationHelper.buildObservationResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.SpecialTypes.ObservationType;
import com.sdc.parser.Config.SpecialTypes.ResponseType;
import com.sdc.parser.Resource.ObservationHelper;

import ca.uhn.fhir.context.FhirContext;

public class FormParser {

	private static final String TEXT_PARSING_ERROR_MSG = "ERROR. TextQuestion type is not accounted for!!!!!";

	public static List<Observation> parseSDCForm(Document document, FhirContext ctx, ConfigValues configValues) {
		// get forminstanceVersion and ID
		String Id = getFormID(document);
		System.out.println("Form ID: " + Id);
		// get Body node
		Node body = getBodyElement(document);
		// get all the children of the body node
		ArrayList<Node> childrenOfBody = getAllChildrenFromBody(body);
		System.out.println("# of children in Body: " + childrenOfBody.size());
		// there should be only 1 child in the body - "ChildItems"
		Element childItems = (Element) childrenOfBody.get(0);
		NodeList questionList = getAllQuestionNodes(childItems);
		List<Observation> answeredQuestions = getAnsweredQuestions(questionList, Id, ctx, configValues);
		List<Section> allSections = ParserHelper.nodeListToElemArray(childItems.getChildNodes()).stream().filter(isSection())
				.map(elem -> convertElemToSection(elem)).filter(elem -> elem != null).map(Section::flatten).flatMap(Collection::stream).toList();

		List<Observation> matchedSections = getMatchingSections(configValues, answeredQuestions, allSections);
		List<Observation> unmatchedSections = allSections.stream().filter(section -> !matchedSections.stream().anyMatch(sectionMatches(section)))
				.map(Section::toObservation).toList();
				
		System.out.println("# of questions: " + questionList.getLength());

		return Stream.of(answeredQuestions,
				ObservationHelper.populateRelationHierarchies(allSections, Stream.of(matchedSections, unmatchedSections).flatMap(Collection::stream).toList()))
				.flatMap(Collection::stream).toList();
	}

	private static List<Observation> getMatchingSections(ConfigValues configValues, List<Observation> answeredQuestions, List<Section> allSections) {
		List<Observation> matchedSections = new ArrayList<>();
		answeredQuestions.forEach(question -> {
			String capQuestionId = question.getCode().getCoding().stream().filter(coding -> coding.getSystem().equals(configValues.getSystemName()))
					.map(Coding::getCode).findAny().orElse("Unknown");
			Section matchingSection = getQuestionsSection(allSections, capQuestionId);
			Observation secObservation;
			if (matchingSection != null) {
				if (matchedSections.stream().anyMatch(sectionMatches(matchingSection))) {
					secObservation = matchedSections.stream().filter(sectionMatches(matchingSection)).findFirst().get();
				} else {
					secObservation = matchingSection.toObservation(configValues.getSystemName());
					matchedSections.add(secObservation);
				}
				ObservationHelper.addDerivedAndMemberRelations(secObservation, question);
			}
		});
		return matchedSections;
	}

	private static Predicate<? super Observation> sectionMatches(Section matchingSection) {
		return (Predicate<? super Observation>) sectionObservation -> sectionObservation != null
				&& sectionObservation.getCode().getCodingFirstRep().getCode().equals(matchingSection.getID());
	}

	private static Section getQuestionsSection(List<Section> sectionList, String capQuestionId) {
		return sectionList.stream().filter(section -> {
			Question matchingQuestion = getMatchingQuestion(section, capQuestionId);
			if (matchingQuestion == null) {
				Section matchingSection = getQuestionsSection(section.getSubSections(), capQuestionId);
				if (matchingSection != null) {
					matchingQuestion = getMatchingQuestion(matchingSection, capQuestionId);
				}
			}
			return matchingQuestion != null;
		}).findAny().orElse(null);
	}

	private static Question getMatchingQuestion(Section section, String capQuestionId) {
		if (section == null) {
			return null;
		}
		return section.getQuestions().stream().filter(secQuestion -> secQuestion.getID().equals(capQuestionId)).findAny().orElse(null);
	}

	private static Section convertElemToSection(Element sectionElem) {
		try {
			return new Section(sectionElem);
		} catch (Exception e) {
			System.out.println("Cannot cast to Section Object");
			return null;
		}
	}

	/**
	 * This will traverse through the list of selected questions
	 * 
	 * @param questionList
	 * @param Id
	 * @param ctx
	 * @return
	 */
	public static ArrayList<Observation> getAnsweredQuestions(NodeList questionList, String Id, FhirContext ctx, ConfigValues configValues) {

		ArrayList<Observation> observations = new ArrayList<Observation>();
		for (int i = 0; i < questionList.getLength(); i++) {
			Element questionElement = (Element) questionList.item(i);
			String questionID = questionElement.getAttribute("ID");

			// Subquestions get handled during the parent-question handling. This prevents repeated observations
			if (isObservationAlreadyHandled(observations, questionID)) {
				continue;
			}
			// get the listFieldElement
			boolean isListQuestion = isQuestionAListQuestion(questionElement);
			boolean isTextQuestion = isQuestionATextQuestion(questionElement);
			if (isListQuestion) {
				boolean isMultiSelect = getListFieldEelementToCheckForMultiSelect(questionElement);
				// get the ListItems under this question where selected = true
				NodeList listItemList = questionElement.getElementsByTagName("ListItem");
				if (!isMultiSelect) {
					for (int j = 0; j < listItemList.getLength(); j++) {
						Element listItemElement = (Element) listItemList.item(j);
						if (listItemElement.hasAttribute("selected")) {
							Element parentQuestion = (Element) listItemElement.getParentNode().getParentNode().getParentNode();
							if (parentQuestion.getAttribute("ID").equals(questionElement.getAttribute("ID"))) {
								System.out.println("QUESTION.ID: " + questionElement.getAttribute("ID"));
								System.out.println("LISTITEM.ID: " + listItemElement.getAttribute("ID"));
								System.out.println("LISTITEM.TITLE: " + listItemElement.getAttribute("title"));
								System.out.println("*******************************************************************");
								// observation = buildListObservationResource(questionElement, listItemElement,
								// Id, ctx);
								observations.addAll(buildObservationResources(ObservationType.LIST, null, questionElement,
										new ArrayList<>(Arrays.asList(listItemElement)), null, null, Id, ctx, configValues));
							}

						}
					}
				} else {

					ArrayList<Element> listElementsAnswered = new ArrayList<Element>();
					// check if there are any selected answers before hand!
					for (int j = 0; j < listItemList.getLength(); j++) {
						Element listItemElement = (Element) listItemList.item(j);
						if (listItemElement.hasAttribute("selected")) {
							Element parentQuestion = (Element) listItemElement.getParentNode().getParentNode().getParentNode();
							if (parentQuestion.getAttribute("ID").equals(questionID)) {
								listElementsAnswered.add(listItemElement);
							}
						}
					}

					// Now if there are selected answers then only add them as components
					if (!listElementsAnswered.isEmpty()) {
						// observation = buildMultiSelectObservationResource(questionElement, Id, ctx);
						observations.addAll(buildObservationResources(ObservationType.MULTISELECT, null, questionElement, listElementsAnswered, null, null, Id, ctx,
								configValues));
						for (Observation observation : observations) {
							String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
							System.out.println(encoded);
							System.out.println("*******************************************************************");
						}
					}
				}
			} else if (isTextQuestion) {
				Element textQuestionResponse = getQuestionResponse(questionElement);
				Element questionResponseUnits = getQuestionResponseUnits(questionElement);
				ResponseType responseType = null;

				for (ResponseType type : ResponseType.values()) {
					String typeAsString = type.name().toLowerCase();
					Element textQuestionOfType = getResponseString(typeAsString, textQuestionResponse);
					// Type is accounted for
					if (textQuestionOfType != null) {
						responseType = type;
						if (isQuestionResponseEmpty(textQuestionOfType)) {
							// no response so don't store the observation
							break;
						}
						String response = getTextResponseForType(typeAsString, textQuestionResponse);

						observations.addAll(buildObservationResources(ObservationType.TEXT, type, questionElement, null, response, questionResponseUnits, Id, ctx, configValues));
						break;
					}
				}
				if (responseType == null) {
					System.out.println(TEXT_PARSING_ERROR_MSG);
				}
			} else {
				System.out.println("Question NOT List or Text");
				System.out.println("QUESTION.ID: " + questionElement.getAttribute("ID"));
			}
		}
		return observations;
	}

	private static boolean isObservationAlreadyHandled(ArrayList<Observation> observations, String questionID) {
		boolean observationAlreadyHandled = false;
		for (Observation observation : observations) {
			observationAlreadyHandled = observation.getCode().getCoding().get(0).getCode() == questionID;
			if (observationAlreadyHandled) {
				break;
			}
		}
		return observationAlreadyHandled;
	}

}
