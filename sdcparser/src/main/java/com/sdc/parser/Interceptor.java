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

import static com.sdc.parser.Bundle.BundleHelper.createBundle;
import static com.sdc.parser.FormParser.parseSDCForm;
import static com.sdc.parser.ParserHelper.getTimeStamp;
import static com.sdc.parser.ParserHelper.getUUID;

import java.util.List;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

import com.sdc.parser.Config.ConfigValues;

import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;

@Path("/")
public class Interceptor {

	/**
	 *
	 */
	private static final String SERVER_URL_ERROR = "There is something wrong with the URL of the server!!!!!";
	private final String LANDING_MESSAGE = "<h1>Welcome to Canada Health Infoway's SDC Parser Service</h1>"
			+ "<p></p><h3> Optional Paremeters: server = [FHIR Server endpoint the resources will be posted to]</h3>"
			+ "<h3>Ex: /sdcparser?server=http://test.fhir.org/r4</h3>"
			+ "<p></p><h3> Optional Paremeters: format = json/xml </h3>"
			+ "<h3>Ex: /sdcparser?server=http://test.fhir.org/r4&format=json</h3>";
	private final String provenanceHeaderDefault = "{\"resourceType\": \"Provenance\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"TIME_STAMP\"},\"recorded\": \"TIME_STAMP\",\"agent\": [{\"type\": {\"text\": \"Joel and Alex testing\"}}]}";
	FhirContext ctx;
	ConfigValues configValues;

	public Interceptor() throws IOException {
		this.ctx = FhirContext.forR4();
		this.configValues = new ConfigValues();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHello() {
		return LANDING_MESSAGE;
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public String loadXMLFromString(String sdcForm, @QueryParam("server") String server,
			@QueryParam("format") String format, @QueryParam("bundleType") String bundleType) {
		if (format == null || format.isEmpty() || format.equals("")) {
			format = "xml";
		}
		if (bundleType == null || bundleType.isEmpty() || bundleType.equals("")) {
			bundleType = "default";
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
				return SERVER_URL_ERROR;
			}
			String provenanceHeader = provenanceHeaderDefault.replaceAll(Pattern.quote("TIME_STAMP"), getTimeStamp());
			AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
			String type = switch (format.toLowerCase()) {
				case "json" -> "json";
				default -> "xml";
			};
			interceptor.addHeaderValue("Content-Type", "application/fhir+" + type);
			interceptor.addHeaderValue("X-Provenance", provenanceHeader);
			client = ctx.newRestfulGenericClient(url.toString());
			ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
			// client.registerInterceptor(interceptor);

		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(sdcForm));
			Document document = builder.parse(is);
			ArrayList<Observation> observations = parseSDCForm(document, ctx, configValues);

			// TODO: Parse reference list
			List<Reference> ref = null;

			// create bundle
			String patientUUID = getUUID();
			String practUUID = getUUID();
			String practRoleUUID = getUUID();
			String docRefUUID = getUUID();
			String messageHeaderUUID = getUUID();
			String diagRepUUID = getUUID();
			Bundle bundle = createBundle(observations, ctx, sdcForm, document, patientUUID, practUUID, practRoleUUID,
					docRefUUID,
					messageHeaderUUID, diagRepUUID, ref, configValues);
			String encoded = null;
			if (format.equalsIgnoreCase("xml")) {
				encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
			} else {
				encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
			}

			if (noServer) {
				stringbuilder.append(encoded);
			} else {
				MethodOutcome resp = client.create()
						.resource(bundle)
						.prettyPrint()
						.encodedJson()
						.execute(); // client.transaction().withBundle(bundle).execute();
				if (resp.getCreated()) {
					IIdType id = resp.getId();
					String created = "Bundle created with Id :  " + id;
					stringbuilder.append(created);
					// if (format.equalsIgnoreCase("json")) {

					// } else {
					// stringbuilder.append(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resp.getResource()));
					// }
				} else {
					stringbuilder.append("Something went horribly wrong! What are we going to do?");
				}
			}
		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
		if (bundleType.equals("transaction")) {
			return "Transaction Bundle Type In Development...";
		} else {
			return stringbuilder.toString();
	}
	}

}
