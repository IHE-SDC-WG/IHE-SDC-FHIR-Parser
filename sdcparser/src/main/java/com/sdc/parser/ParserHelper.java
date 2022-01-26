package com.sdc.parser;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserHelper {
	private ParserHelper() {
		// restrict instantiation
	}

	/**
	 * Method that gets the root Element - SDCPackage
	 * 
	 * @param document
	 * @return
	 */
	public static Element getRootElement(Document document) {
		Element root = document.getDocumentElement(); // SDCPackage
		System.out.println("Root: " + root.getNodeName());
		return root;
	}

	/**
	 * Method that return the Body Element
	 * 
	 * @param document
	 * @return
	 */
	public static Node getBodyElement(Document document) {
		Node body = null;
		NodeList nList = document.getElementsByTagName("Body");
		body = nList.item(0);
		return body;
	}

	/**
	 * Method that get the formInstranceVrsionURI and ID
	 * 
	 * @param document
	 * @return
	 */
	public static String getFormID(Document document) {
		Element root = getRootElement(document);
		NodeList nodeList = root.getElementsByTagName("FormDesign");
		Element formDesignNode = (Element) nodeList.item(0);
		return formDesignNode.getAttribute("ID") + formDesignNode.getAttribute("formInstanceVersionURI");
	}
	
	/**
	 * Method that get the form title
	 * 
	 * @param document
	 * @return the form's title as String
	 */
	public static String getFormTitle(Document document) {
		Element root = getRootElement(document);
		NodeList nodeList = root.getElementsByTagName("FormDesign");
		Element formDesignNode = (Element) nodeList.item(0);
		return formDesignNode.getAttribute("formTitle");
	}

	/**
	 * Method that gets the Children of the Body element. There should be only 1
	 * ChildItems
	 * 
	 * @param body
	 * @return
	 */
	public static ArrayList<Node> getAllChildrenFromBody(Node body) {
		NodeList children = body.getChildNodes();
		return removeWhiteSpaces(children);
	}

	/**
	 * Method that gets all the sections form the ChildItems under Body
	 * 
	 * @param childItems
	 * @return
	 */
	public static NodeList getSectionsFromChildItems(Element childItems) {
		NodeList nodeList = childItems.getElementsByTagName("Section");
		return nodeList;
	}

	/**
	 * Method that removes all the "# text" elements form the list
	 * 
	 * @param nodeList
	 * @return
	 */
	public static ArrayList<Node> removeWhiteSpaces(NodeList nodeList) {
		ArrayList<Node> returnList = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				returnList.add(node);
			}
		}

		return returnList;
	}

	public static NodeList getAllQuestionNodes(Element childItems) {
		NodeList questionList = childItems.getElementsByTagName("Question");
		return questionList;
	}

	public static boolean isQuestionAListQuestion(Element questionElement) {
		NodeList listFieldElementList = questionElement.getElementsByTagName("ListField");
		if (listFieldElementList.getLength() > 0) {
			return true;
		}
		return false;
	}

	public static boolean isQuestionATextQuestion(Element questionElement) {
		NodeList responseFieldElementList = questionElement.getElementsByTagName("ResponseField");
		if (responseFieldElementList.getLength() > 0) {
			return true;
		}
		return false;
	}

	public static boolean getListFieldEelementToCheckForMultiSelect(Element questionElement) {
		NodeList listFieldElementList = questionElement.getElementsByTagName("ListField");
		if (listFieldElementList.getLength() > 0) {
			Element listFieldElement = (Element) listFieldElementList.item(0);
			if (listFieldElement.hasAttribute("maxSelections")) {
				return true;
			}
		}
		return false;
	}

	public static Element getTextQuestionResponse(Element questionElement) {
		Element textQuestionElement = (Element) questionElement.getElementsByTagName("ResponseField").item(0);
		Element responseElement = (Element) textQuestionElement.getElementsByTagName("Response").item(0);
		return responseElement;
	}

	public static String getTextResponseForType(String type, Element textQuestionResponse) {
		Element integerElement = (Element) textQuestionResponse.getElementsByTagName(type).item(0);
		return integerElement.getAttribute("val");
	}

	/**
	 * @param type
	 * @param textQuestionResponse
	 * @return The textQuestion of that type, or else null
	 */
	public static Element getTextQuestionOfType(String type, Element textQuestionResponse) {
		NodeList textElementList = textQuestionResponse.getElementsByTagName(type);
		if (textElementList.getLength() > 0) {
			return (Element) textElementList.item(0);
		}
		return null;
	}

	public static boolean isTextQuestionResponseEmpty(Element textElementResponse) {
		boolean valEmpty = !textElementResponse.hasAttribute("val");
		if (!valEmpty) {
			return textElementResponse.getAttribute("val").length() == 0;
		}
		return valEmpty;
	}

	/**
	 * Generated a Globally Unique Identifier
	 * 
	 * @return
	 */
	public static String getUUID() {
		String uuid = "urn:uuid:" + String.valueOf(UUID.randomUUID());
		return uuid;
	}

	/*
	 * Produces the current time stamp of the instant that it is called
	 */
	public static String getTimeStamp() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Instant instant = timestamp.toInstant();
		return instant.toString();
	}
}
