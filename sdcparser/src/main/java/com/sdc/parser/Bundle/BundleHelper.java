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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Resource.MessageHeaderHelper;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r4.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;

public class BundleHelper {

	private static ConceptMap snomedConceptMap;

	private static final String SNOMED_CONCEPTMAP_FILENAME = "CAPCkeyToSNOMEDmapNotFormatted.json";

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
				getMatchingCodes("snomed", coding, ctx).forEach(match -> obs.getCode().addCoding(match));
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
		if (snomedConceptMap == null || snomedConceptMap.isEmpty()) {
			InputStream inputStream = BundleHelper.class.getClassLoader().getResourceAsStream(SNOMED_CONCEPTMAP_FILENAME);
			IBaseResource resource = ctx.newJsonParser().parseResource(inputStream);
			if (resource instanceof ConceptMap) {
				snomedConceptMap = (ConceptMap) resource;
			}
		}

		
		HashMap<String, List<SourceElementComponent>> groupComponents = new HashMap<>();
		snomedConceptMap.getGroup().stream().forEach(grpVal -> {
			if (grpVal.getSource().contains("cap") && grpVal.getTarget().contains(system)) {
				List<SourceElementComponent> compList = new ArrayList<>();
				grpVal.getElement().forEach(elem -> {
					if (elem.getCode().equals(coding.getCode())) {
						compList.add(elem);
					}
				});
				if (!compList.isEmpty()) {
					groupComponents.put(grpVal.getTarget(), compList);
				}
			}
		});


		
		// .stream().filter(grp -> grp.getSource().contains("cap") && grp.getTarget().contains(system)).toList();


		// List<SourceElementComponent> matchedCodes = filteredGroups.getGroup().stream().map(grp -> grp.getElement().stream()
		// 				.filter(elemCode -> elemCode.getCode().equals(coding.getCode())).collect(Collectors.toList()))
		// 		.flatMap(Collection::stream).collect(Collectors.toList());

		// matchedCodes.forEach(mCode -> System.out.println(mCode.getCode()));

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
