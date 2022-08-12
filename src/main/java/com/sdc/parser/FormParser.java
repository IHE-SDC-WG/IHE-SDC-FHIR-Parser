package com.sdc.parser;

import static com.sdc.parser.ParserHelper.getAllChildrenFromBody;
import static com.sdc.parser.ParserHelper.getAllQuestionNodes;
import static com.sdc.parser.ParserHelper.getBodyElement;
import static com.sdc.parser.ParserHelper.getFormID;
import static com.sdc.parser.ParserHelper.getListFieldEelementToCheckForMultiSelect;
import static com.sdc.parser.ParserHelper.getTextQuestionOfType;
import static com.sdc.parser.ParserHelper.getTextQuestionResponse;
import static com.sdc.parser.ParserHelper.getTextResponseForType;
import static com.sdc.parser.ParserHelper.isQuestionAListQuestion;
import static com.sdc.parser.ParserHelper.isQuestionATextQuestion;
import static com.sdc.parser.ParserHelper.isSection;
import static com.sdc.parser.ParserHelper.isTextQuestionResponseEmpty;
import static com.sdc.parser.Resource.ObservationHelper.buildObservationResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Observation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.SpecialTypes.ObservationType;
import com.sdc.parser.Config.SpecialTypes.TextResponseType;

import ca.uhn.fhir.context.FhirContext;

public class FormParser {

	private static final String TEXT_PARSING_ERROR_MSG = "ERROR. TextQuestion type is not accounted for!!!!!";


	public static ArrayList<Observation> parseSDCForm(Document document, FhirContext ctx, ConfigValues configValues) {

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

		Stream<Element> sectionElems = ParserHelper.nodeListToElemArray(childItems.getChildNodes()).stream().filter(isSection());
		List<Section> rootSections = sectionElems
				.map(sectionElem -> {
					return convertElemToSection(sectionElem); 
				}).collect(Collectors.toList()).stream().filter(elem -> elem != null).toList();

		System.out.println("# of questions: " + questionList.getLength());
		// get the list of questions with selected = "true";
		ArrayList<Observation> answeredQuestions = getAnsweredQuestions(questionList, Id, ctx, configValues);
		return answeredQuestions;
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

			//Subquestions get handled during the parent-question handling. This prevents repeated observations
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
							Element parentQuestion = (Element) listItemElement.getParentNode().getParentNode()
									.getParentNode();
							if (parentQuestion.getAttribute("ID").equals(questionElement.getAttribute("ID"))) {
								System.out.println("QUESTION.ID: " + questionElement.getAttribute("ID"));
								System.out.println("LISTITEM.ID: " + listItemElement.getAttribute("ID"));
								System.out.println("LISTITEM.TITLE: " + listItemElement.getAttribute("title"));
								System.out
										.println("*******************************************************************");
								// observation = buildListObservationResource(questionElement, listItemElement,
								// Id, ctx);
								observations
										.addAll(buildObservationResources(ObservationType.LIST, null, questionElement,
												new ArrayList<>(Arrays.asList(listItemElement)), null, Id, ctx, configValues));
							}

						}
					}
				} else {

					ArrayList<Element> listElementsAnswered = new ArrayList<Element>();
					// check if there are any selected answers before hand!
					for (int j = 0; j < listItemList.getLength(); j++) {
						Element listItemElement = (Element) listItemList.item(j);
						if (listItemElement.hasAttribute("selected")) {
							Element parentQuestion = (Element) listItemElement.getParentNode().getParentNode()
									.getParentNode();
							if (parentQuestion.getAttribute("ID").equals(questionID)) {
								listElementsAnswered.add(listItemElement);
							}
						}
					}

					// Now if there are selected answers then only add them as components
					if (!listElementsAnswered.isEmpty()) {
						// observation = buildMultiSelectObservationResource(questionElement, Id, ctx);
						observations
								.addAll(buildObservationResources(ObservationType.MULTISELECT, null, questionElement,
										listElementsAnswered, null, Id, ctx, configValues));
						for (Observation observation : observations) {
							String encoded = ctx.newXmlParser().setPrettyPrint(true)
									.encodeResourceToString(observation);
							System.out.println(encoded);
							System.out.println("*******************************************************************");
						}
					}
				}
			} else if (isTextQuestion) {
				Element textQuestionResponse = getTextQuestionResponse(questionElement);
				TextResponseType responseType = null;

				for (TextResponseType type : TextResponseType.values()) {
					String typeAsString = type.name().toLowerCase();
					Element textQuestionOfType = getTextQuestionOfType(typeAsString, textQuestionResponse);
					// Type is accounted for
					if (textQuestionOfType != null) {
						responseType = type;
						if (isTextQuestionResponseEmpty(textQuestionOfType)) {
							// no response so don't store the observation
							break;
						}
						String response = getTextResponseForType(typeAsString, textQuestionResponse);
						observations.addAll(buildObservationResources(ObservationType.TEXT, type, questionElement, null,
								response, Id, ctx, configValues));
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
