package com.sdc.parser;

import static com.sdc.parser.Constants.Constants.BOOLEAN;
import static com.sdc.parser.Constants.Constants.DATE;
import static com.sdc.parser.Constants.Constants.DATETIME;
import static com.sdc.parser.Constants.Constants.DECIMAL;
import static com.sdc.parser.Constants.Constants.INTEGER;
import static com.sdc.parser.Constants.Constants.STRING;
import static com.sdc.parser.Constants.Constants.TEXT_PARSING_ERROR_MSG;
import static com.sdc.parser.ParserHelper.*;
import static com.sdc.parser.Resource.ObservationHelper.*;

import java.util.ArrayList;

import com.sdc.parser.Config.ConfigValues;

import org.hl7.fhir.r4.model.Observation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.fhir.context.FhirContext;

public class FormParser {
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
		System.out.println("# of questions: " + questionList.getLength());
		// get the list of questions with selected = "true";
		ArrayList<Observation> answeredQuestions = getAnsweredQuestions(questionList, Id, ctx, configValues);
		return answeredQuestions;
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
		Observation observation = null;
		for (int i = 0; i < questionList.getLength(); i++) {
			Element questionElement = (Element) questionList.item(i);
			String questionID = questionElement.getAttribute("ID");
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
								observation = buildObservationResource(questionElement, listItemElement, Id, ctx);
								observations.add(observation);
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
						observation = buildMultiSelectObservationResource(questionElement, Id, ctx);
						observations.add(observation);
						addComponentToObservation(observation, listElementsAnswered);
						String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
						System.out.println(encoded);
						System.out.println("*******************************************************************");
					}
				}
			} else if (isTextQuestion) {
				Element textQuestion = getTextQuestion(questionElement);
				Element textQuestionResponse = getTextQuestionResponse(textQuestion);
				if (isTextQuestionOfTypeAndHasResponse(INTEGER, textQuestionResponse)) {
					buildAndAddObservationForType(INTEGER, textQuestionResponse, questionElement, Id, ctx,
							observations, configValues);
				} else if (isTextQuestionOfTypeAndHasResponse(DECIMAL, textQuestionResponse)) {
					buildAndAddObservationForType(DECIMAL, textQuestionResponse, questionElement, Id, ctx,
							observations, configValues);
				} else if (isTextQuestionOfTypeAndHasResponse(STRING, textQuestionResponse)) {
					buildAndAddObservationForType(STRING, textQuestionResponse, questionElement, Id, ctx, observations, configValues);
				} else if (isTextQuestionOfTypeAndHasResponse(BOOLEAN, textQuestionResponse)) {
					buildAndAddObservationForType(BOOLEAN, textQuestionResponse, questionElement, Id, ctx,
							observations, configValues);
				} else if (isTextQuestionOfTypeAndHasResponse(DATE, textQuestionResponse)) {
					System.out.println("Question is date");
				} else if (isTextQuestionOfTypeAndHasResponse(DATETIME, textQuestionResponse)) {
					buildAndAddObservationForType(DATETIME, textQuestionResponse, questionElement, Id, ctx,
							observations, configValues);
				} else {
					System.out.println(TEXT_PARSING_ERROR_MSG);
				}
			} else {
				System.out.println("Question NOT List or Text");
				System.out.println("QUESTION.ID: " + questionElement.getAttribute("ID"));
			}
		}
		return observations;
	}

}
