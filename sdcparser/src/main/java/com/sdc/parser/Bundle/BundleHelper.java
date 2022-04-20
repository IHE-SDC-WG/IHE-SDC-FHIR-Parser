package com.sdc.parser.Bundle;

import static com.sdc.parser.ParserHelper.getUUID;
import static com.sdc.parser.Resource.DiagnosticReportHelper.createDiagnosticReport;
import static com.sdc.parser.Resource.PatientHelper.createPatient;
import static com.sdc.parser.Resource.PractitionerHelper.createPractitioner;
import static com.sdc.parser.Resource.PractitionerHelper.generatePractitionerDisplay;
import static com.sdc.parser.Resource.PractitionerRoleHelper.createPractitionerRolePractitioner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Resource.MessageHeaderHelper;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BundleHelper {

	private static final String SNOMED_CONCEPTMAP_FILENAME = "/Skin.Melanoma.Bmk.json";

	public static Bundle createBundle(String bundleType, ArrayList<Observation> observations, FhirContext ctx, String sdcForm,
			ConfigValues configValues) throws IOException {

		Bundle parentBundle = new Bundle();
		parentBundle.setId(getUUID());
		BundleType type;
		ArrayList<BundleEntryComponent> entries = new ArrayList<>();
		BundleEntryComponent practitionerEntry = createBundleEntry(getUUID(), createPractitioner(configValues.getPractitionerConfig()));

		entries.add(createBundleEntry(getUUID(), createPatient(configValues.getPatientConfig())));
		entries.add(practitionerEntry);
		entries.add(createBundleEntry(getUUID(), createPractitionerRolePractitioner(ctx)));
		entries.add(createBundleEntry(getUUID(), createDiagnosticReport(ctx, sdcForm, getUUID(), observations, configValues)));

		// Hydrate Observations
		observations.forEach(obs -> {
			obs.setSubject(new Reference(getUUID()));
			obs.setPerformer(new ArrayList<Reference>(Arrays.asList(
					new Reference(practitionerEntry.getFullUrl())
							.setType(practitionerEntry.getResource().getResourceType().name())
							.setDisplay(generatePractitionerDisplay((Practitioner) practitionerEntry.getResource())))));
			obs.setEffective(new Period().setStart(new Date()));
			obs.getCode().getCoding().forEach(coding -> {
				getMatchingCodes("SNOMED", coding, ctx).forEach(match -> obs.getCode().addCoding(match));
			});
		});
		observations.stream().forEach(obs -> entries.add(createBundleEntry(getUUID(), obs)));

		switch (bundleType) {
			case "transaction":
				type = BundleType.TRANSACTION;
				addEntriesToBundle(parentBundle, entries);
				parentBundle.getEntry().forEach(entry -> {
					// Add Request Resource
					entry.setRequest(
							new BundleEntryRequestComponent()
									.setUrl(entry.getResource().fhirType())
									.setMethod(HTTPVerb.POST));
				});
				break;
		
			default:		
				type = BundleType.MESSAGE;

				// Add message header
				parentBundle.addEntry(createBundleEntry(getUUID(), new MessageHeaderHelper().create(ctx)));

				// Add Content Bundle
				Bundle contentBundle = new Bundle();
				contentBundle.setType(BundleType.COLLECTION);
				addEntriesToBundle(contentBundle, entries);
				parentBundle.addEntry(createBundleEntry(getUUID(), contentBundle));
				break;
		}

		parentBundle.setType(type);

		return parentBundle;
	}

	private static List<Coding> getMatchingCodes(String system, Coding coding, FhirContext ctx) {
		String filename = "Skin.Melanoma.Bmk.json";
		InputStream inputStream = BundleHelper.class.getClassLoader().getResourceAsStream(filename);
		IBaseResource resource = ctx.newJsonParser().parseResource(inputStream);
		if (resource instanceof ConceptMap) {
			ConceptMap cp = (ConceptMap) resource;
			System.out.println(cp.getDescription());
		}

		return new ArrayList<Coding>();
	}

	private static void addEntriesToBundle(Bundle bundle, ArrayList<BundleEntryComponent> entries) {
		entries.forEach(entry -> bundle.addEntry(entry));
	}

	public static BundleEntryComponent createBundleEntry(String fullUrl, Resource resource) {
		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
		bundleEntryComponent.setFullUrl(fullUrl);
		bundleEntryComponent.setResource(resource);
		return bundleEntryComponent;
	}

}
