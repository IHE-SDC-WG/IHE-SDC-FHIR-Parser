/*
 * MIT License

Copyright (c) 2020 Canada Health Infoway

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */


package com.sdc.parser;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;

@Path("/")
public class Interceptor {

	FhirContext ctx;

	public Interceptor() {
		this.ctx = FhirContext.forR4();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHello() {
		return "<h1>Welcome to Canada Health Infoway's SDC Parser Service</h1>"
				+ "<p></p><h3> Optional Paremeters: server = [FHIR Server endpoint the resources will be posted to]</h3>" +
				"<h3>Ex: /sdcparser?server=http:/test.fhir.org/r4</h3>";
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public String loadXMLFromString(String sdcForm, @QueryParam("server") String server) {
		System.out.println("this is what I got: \n" + sdcForm);
		StringBuilder stringbuilder = new StringBuilder();
		boolean noServer = true;
		IGenericClient client = null;
		URL url = null;
		if (server == null || server.isEmpty()) {
			noServer = true;
		} else {
			noServer = false;
			try {
				url = new URL(server);
			} catch (MalformedURLException mfe) {
				return "There is something wrong with the URL of the server!!!!!";
			}
			client = ctx.newRestfulGenericClient(url.toString());
			ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(sdcForm));
			Document document = builder.parse(is);
			ArrayList<Observation> observations = parseSDCForm(document, ctx);
			int obsListSize = observations.size();
			for (int i = 0; i < obsListSize; i++) {
				Observation obs = observations.get(i);
				if (!noServer) {
					MethodOutcome outcome = client.create().resource(obs).execute();
					if (outcome.getCreated()) {
						IIdType id = outcome.getId();
						obs.setId(id);
					}
				}
				String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observations.get(i));
				stringbuilder.append(encoded + "\n************************************************************\n");
			}
		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
		return stringbuilder.toString();
	}

	public static ArrayList<Observation> parseSDCForm(Document document, FhirContext ctx) {

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
		ArrayList<Observation> answeredQuestions = getSelectedTrueQuestions(questionList, Id, ctx);
		return answeredQuestions;
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
		Element root = getRootElement(document);
		NodeList nList = document.getElementsByTagName("Body");
		body = nList.item(0);
		return body;
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

	public static void printQuestionName(ArrayList<Node> questionList) {
		for (int i = 0; i < questionList.size(); i++) {
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
	public static ArrayList<Observation> getSelectedTrueQuestions(NodeList questionList, String Id, FhirContext ctx) {

		ArrayList<Observation> observations = new ArrayList<Observation>();
		Observation observation = null;
		for (int i = 0; i < questionList.getLength(); i++) {
			Element questionElement = (Element) questionList.item(i);
			// get the listFieldElement
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
							System.out.println("*******************************************************************");
							observation = buildObservationResource(questionElement, listItemElement, Id, ctx);
							observations.add(observation);
						}

					}
				}
			} else {

				String questionID = questionElement.getAttribute("ID");
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
		}
		return observations;
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

	public static Observation buildObservationResource(Element questionElement, Element listItemElement, String id,
			FhirContext ctx) {

		Observation observation = new Observation();
		observation.setSubject(new Reference("Patient/6754"));
		observation.addPerformer().setReference("Practitioner/pathpract1");
		observation.addIdentifier().setSystem("https://CAP.org")
				.setValue(id + "#" + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem("https://CAP.org").setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		observation.setValue(new CodeableConcept()).getValueCodeableConcept().addCoding().setSystem("https://CAP.org")
				.setCode(listItemElement.getAttribute("ID")).setDisplay(listItemElement.getAttribute("title"));
		observation.addDerivedFrom().setReference("DocumentReference/" + id);
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
		System.out.println(encoded);
		System.out.println("*******************************************************************");
		return observation;

	}

	public static Observation buildMultiSelectObservationResource(Element questionElement, String id, FhirContext ctx) {
		Observation observation = new Observation();
		observation.addIdentifier().setSystem("https://CAP.org")
				.setValue(id + "." + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem("https://CAP.org").setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		observation.addDerivedFrom().setReference("DocumentReference/" + id);
		return observation;
	}

	public static Observation addComponentToObservation(Observation observation,
			ArrayList<Element> listElementsAnswered) {

		for (Element element : listElementsAnswered) {

			observation.addComponent().getCode().addCoding().setSystem("https://CAP.org")
					.setCode(element.getAttribute("ID")).setDisplay(element.getAttribute("title"));
		}

		return observation;
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

}
