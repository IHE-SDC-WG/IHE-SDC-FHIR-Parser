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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

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

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.codesystems.ProvenanceAgentRole;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;

@Path("/")
public class Interceptor {

	FhirContext ctx;
	String provenanceHeader = "{\"resourceType\": \"Provenance\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"2020-08-31T20:44:24.994+00:00\"},\"recorded\": \"2020-05-14T13:44:24.1703291-07:00\",\"agent\": [{\"type\": {\"text\": \"Joel and Alex testing\"}}]}";
	final static String INTEGER = "integer";
	final static String DECIMAL = "decimal";
	final static String STRING = "string";
	final static String BOOLEAN = "boolean";
	final static String DATE = "date";
	final static String DATETIME = "dateTime";

	public Interceptor() {
		this.ctx = FhirContext.forR4();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHello() {
		return "<h1>Welcome to Canada Health Infoway's SDC Parser Service</h1>"
				+ "<p></p><h3> Optional Paremeters: server = [FHIR Server endpoint the resources will be posted to]</h3>" +
				"<h3>Ex: /sdcparser?server=http://test.fhir.org/r4</h3>" + 
				"<p></p><h3> Optional Paremeters: format = json/xml </h3>" +
				"<h3>Ex: /sdcparser?server=http://test.fhir.org/r4&format=json</h3>";
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String loadXMLFromString(String sdcForm, @QueryParam("server") String server, @QueryParam("format") String format) {
		if(format == null || format.isEmpty() || format.equals("")) {
			format = "xml";
		}
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
			String provenanceHeader = "{\"resourceType\": \"Provenance\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"TIME_STAMP\"},\"recorded\": \"TIME_STAMP\",\"agent\": [{\"type\": {\"text\": \"Joel and Alex testing\"}}]}";
			provenanceHeader = provenanceHeader.replaceAll(Pattern.quote("TIME_STAMP"), getTimeStamp());
			AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
			if(format.equalsIgnoreCase("xml")){
				interceptor.addHeaderValue("Content-Type", "application/fhir+xml");
			}
			else if(format.equalsIgnoreCase("json")) {
				interceptor.addHeaderValue("Content-Type", "application/fhir+json");
			}
			else {
				interceptor.addHeaderValue("Content-Type", "application/fhir+xml");
			}
			interceptor.addHeaderValue("X-Provenance", provenanceHeader);
			client = ctx.newRestfulGenericClient(url.toString());
			ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
			//client.registerInterceptor(interceptor);
			
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(sdcForm));
			Document document = builder.parse(is);
			ArrayList<Observation> observations = parseSDCForm(document, ctx);
			//create bundle
			String patientUUID = getUUID(); 
			String docRefUUID = getUUID();
			Bundle bundle = createBundle(observations, ctx, sdcForm, document, patientUUID, docRefUUID);
			String encoded = null;
			if(format.equalsIgnoreCase("xml")) {
				encoded  = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
			}
			else {
				encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
			}
					
			if(noServer) {
				stringbuilder.append(encoded);
			}
			else {
				Bundle resp = client.transaction().withBundle(bundle).execute();
				if(format.equalsIgnoreCase("json")) {
					stringbuilder.append(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));
				}
				else {
					stringbuilder.append(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));
				}
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
	
	public static Bundle createBundle(ArrayList<Observation> Observations, FhirContext ctx, String sdcForm, Document form, String patientUUID, String docRefUUID) {
		Bundle bundle = new Bundle();
		String bundleUUID = getUUID();  
		bundle.setId(bundleUUID);
		bundle.setType(BundleType.MESSAGE);
		//Add message header
		BundleEntryComponent messageHeader = new BundleEntryComponent();
		String messageHeaderUUID = getUUID();
		messageHeader.setFullUrl(messageHeaderUUID);
		messageHeader.setResource(createMessageHeader(ctx));
		bundle.addEntry(messageHeader);
		//Add patient resource
		BundleEntryComponent patient = new BundleEntryComponent();
		patient.setFullUrl(patientUUID);
		patient.setResource(createPatient(ctx));
		bundle.addEntry(patient);
		//Add document reference resource
		BundleEntryComponent docRef = new BundleEntryComponent();
		docRef.setFullUrl(docRefUUID);
		docRef.setResource(createDocReference(ctx, sdcForm, form ,patientUUID));
		bundle.addEntry(docRef);
		//add observations
		for(Observation obs: Observations) {
			obs.setSubject(new Reference( patientUUID ));
			obs.addDerivedFrom().setReference(docRefUUID);
			BundleEntryComponent bec = new BundleEntryComponent();
			bec.setFullUrl(getUUID());
			bec.setResource(obs);
			bundle.addEntry(bec);
		}
		return bundle;
	}
	
	public static MessageHeader createMessageHeader(FhirContext ctx) {
		MessageHeader messageHeader = new MessageHeader();
		Narrative messageHeaderNarrative = new Narrative();
		messageHeaderNarrative.setStatus(NarrativeStatus.GENERATED);
		XhtmlNode messageHeaderText = new XhtmlNode(NodeType.Element, "p");
		messageHeaderText.addText("SDC example Transaction Bundle for fake patient Jose incoduing Patient, Encounter");
		messageHeaderNarrative.setDiv(messageHeaderText);
		messageHeader.setText(messageHeaderNarrative);
		messageHeader.getEventCoding().setSystem("http://example.org/fhir/message-events").setCode("admin-notify");
		messageHeader.setSource(new MessageSourceComponent().setName("IHE SDC on FHIR Parser").setEndpoint("http://localhost:8080/sdcparser"));
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(messageHeader);
		return messageHeader;
	}

	public static Patient createPatient(FhirContext ctx) {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("JoelAlexPatient");
		patient.addName().setFamily("Rodriguez").addGiven("Jose");
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}
	public static Practitioner createPractitioner(FhirContext ctx) {
		Practitioner pract = new Practitioner();
		pract.addName().setFamily("Bit").addGiven("Rex");
		pract.addIdentifier().setSystem("http://someIdentifier.com").setValue("pathpract1");
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(pract);
		return pract;
	}
	public static Provenance createProvenance(FhirContext ctx, String bundleUUID) {
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date dateobj = new Date();
		Provenance provenance = new Provenance();
		provenance.setRecorded(dateobj);
		Type type = new DateTimeType();
		provenance.setOccurred(type);
		provenance.getTargetFirstRep().setReference("Bundle/" + bundleUUID);
		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		pac.getRoleFirstRep().getCodingFirstRep().setCode(ProvenanceAgentRole.ASSEMBLER.toString())
		.setDisplay(ProvenanceAgentRole.ASSEMBLER.getDisplay()).setSystem("http://terminology.hl7.org/CodeSystem/provenance-participant-type");
		provenance.addAgent(pac);
		return provenance;
	}
	public static DocumentReference createDocReference(FhirContext ctx, String sdcForm, Document form,  String patientUUID) {
		DocumentReference docRef = new DocumentReference();
		Narrative narry = new Narrative();
		narry.setDivAsString("This DocumentReference was created by the Infoway Parser for form " + getFormID(form));
		docRef.setStatus(DocumentReferenceStatus.CURRENT);
		docRef.setText(narry);
		docRef.getMasterIdentifier().setSystem("https://cap.org").setValue(getFormID(form));
		docRef.setSubject(new Reference(patientUUID));
		docRef.setDate(new Date());
		DocumentReferenceContentComponent drcc = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setContentType("text/plain");
		attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
		drcc.setAttachment(attachment);
		docRef.addContent(drcc);
		return docRef;
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
								System.out.println("*******************************************************************");
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
							observations);
				} else if (isTextQuestionOfTypeAndHasResponse(DECIMAL, textQuestionResponse)) {
					buildAndAddObservationForType(DECIMAL, textQuestionResponse, questionElement, Id, ctx,
							observations);
				} else if (isTextQuestionOfTypeAndHasResponse(STRING, textQuestionResponse)) {
					buildAndAddObservationForType(STRING, textQuestionResponse, questionElement, Id, ctx, observations);
				} else if (isTextQuestionOfTypeAndHasResponse(BOOLEAN, textQuestionResponse)) {
					buildAndAddObservationForType(BOOLEAN, textQuestionResponse, questionElement, Id, ctx,
							observations);
				} else if (isTextQuestionOfTypeAndHasResponse(DATE, textQuestionResponse)) {
					System.out.println("Question is date");
				} else if (isTextQuestionOfTypeAndHasResponse(DATETIME, textQuestionResponse)) {
					buildAndAddObservationForType(DATETIME, textQuestionResponse, questionElement, Id, ctx,
							observations);
				} else {
					System.out.println("ERROR. TextQuestion type is not accounted for!!!!!");
				}
			} else {
				System.out.println("Question NOT List or Text");
				System.out.println("QUESTION.ID: " + questionElement.getAttribute("ID"));
			}
		}
		return observations;
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
	
	public static void buildAndAddObservationForType(String type, Element textQuestionResponse, Element questionElement,
			String Id, FhirContext ctx, ArrayList<Observation> observations) {
		String response = getTextResponseForType(type, textQuestionResponse);
		Observation observation = buildTextObservationResource(type, questionElement, response, Id, ctx);
		observations.add(observation);
	}

	public static Element getTextQuestion(Element questionElement) {
		Element textQuestionElement = (Element) questionElement.getElementsByTagName("ResponseField").item(0);
		return textQuestionElement;
	}

	public static Element getTextQuestionResponse(Element textQuestion) {
		Element responseElement = (Element) textQuestion.getElementsByTagName("Response").item(0);
		return responseElement;
	}

	public static String getTextResponseForType(String type, Element textQuestionResponse) {
		Element integerElement = (Element) textQuestionResponse.getElementsByTagName(type).item(0);
		return integerElement.getAttribute("val");
	}

	public static boolean isTextQuestionOfTypeAndHasResponse(String type, Element textQuestionResponse) {
		NodeList dateTimeElementList = textQuestionResponse.getElementsByTagName(type);
		if (dateTimeElementList.getLength() > 0) {
			Element dateTimeElement = (Element) dateTimeElementList.item(0);
			if (dateTimeElement.hasAttribute("val")) {
				return true;
			}
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
	
	public static Observation buildTextObservationResource(String type, Element questionElement, String textResponse,
			String id, FhirContext ctx) {

		Observation observation = new Observation();
		observation.setSubject(new Reference("Patient/6754"));
		observation.addPerformer().setReference("Practitioner/pathpract1");
		observation.addIdentifier().setSystem("https://CAP.org")
				.setValue(id + "#" + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem("https://CAP.org").setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		if (type == INTEGER) {
			observation.setValue(new IntegerType(textResponse)).getValueIntegerType();
		} else if (type == DECIMAL) {
			observation.setValue(new Quantity(Double.parseDouble(textResponse))).getValueQuantity();
		} else if (type == STRING) {
			observation.setValue(new StringType(textResponse)).getValueStringType();
		} else if (type == BOOLEAN) {
			observation.setValue(new BooleanType(textResponse)).getValueBooleanType();
		} else if (type == DATETIME) {
			observation.setValue(new DateTimeType(textResponse)).getValueDateTimeType();
		} else {
			System.out.println("ERROR: BUILDING OBERVATION FOR UNSUPPORTED TYPE");
		}
		observation.addDerivedFrom().setReference("DocumentReference/" + id);
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
		System.out.println(encoded);
		System.out.println("*******************************************************************");
		return observation;
	}

	public static Observation buildObservationResource(Element questionElement, Element listItemElement, String id,
			FhirContext ctx) {

		Observation observation = new Observation();
		//observation.setSubject(new Reference("Patient/6754"));
		observation.addIdentifier().setSystem("https://CAP.org")
				.setValue(id + "#" + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem("https://CAP.org").setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		observation.setValue(new CodeableConcept()).getValueCodeableConcept().addCoding().setSystem("https://CAP.org")
				.setCode(listItemElement.getAttribute("ID")).setDisplay(listItemElement.getAttribute("title"));
		//observation.addDerivedFrom().setReference("DocumentReference/" + id);
		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(observation);
		return observation;

	}

	public static Observation buildMultiSelectObservationResource(Element questionElement, String id, FhirContext ctx) {
		Observation observation = new Observation();
		//observation.setSubject(new Reference("Patient/6754"));
		observation.addIdentifier().setSystem("https://CAP.org")
				.setValue(id + "." + questionElement.getAttribute("ID"));
		observation.setStatus(ObservationStatus.FINAL);
		observation.getCode().addCoding().setSystem("https://CAP.org").setCode(questionElement.getAttribute("ID"))
				.setDisplay(questionElement.getAttribute("title"));
		//observation.addDerivedFrom().setReference("DocumentReference/" + id);
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
	 * Generated a Globally Unique Identifier
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
