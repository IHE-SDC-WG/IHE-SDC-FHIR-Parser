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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import com.sdc.parser.ParserHelper;
import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.PatientConfig;
import com.sdc.parser.Resource.MessageHeaderHelper;

import ca.uhn.fhir.context.FhirContext;

public class BundleHelper {

	private static ConceptMap conceptMap;
	private static final String SNOMED_CONCEPTMAP_FILENAME = "CAPCkeyToSNOMEDmapNotFormatted.json";
	private static ArrayList<BundleEntryComponent> entries;
	private static BundleEntryComponent practitionerEntry;
	private static BundleEntryComponent patientEntry;
	private static Reference patientReference;

	public static Bundle createBundle(String bundleType, ArrayList<Observation> observations, FhirContext ctx, String sdcForm,
			ConfigValues configValues) throws IOException {

		Bundle parentBundle = new Bundle();
		PatientConfig patientConfig = configValues.getPatientConfig();
		parentBundle.setId(getUUID());

		patientEntry = createBundleEntry(getUUID(), createPatient(configValues.getPatientConfig()));
		patientReference = new Reference(
				ParserHelper.createReferenceString(patientEntry.getResource().getResourceType(),
						patientConfig.getIdentifier()))
				.setDisplay(patientConfig.getFullName());
		practitionerEntry = createBundleEntry(getUUID(), createPractitioner(configValues.getPractitionerConfig()));

		hydrateEntries(observations, ctx, sdcForm, configValues);
		hydrateObservations(observations, ctx);

		parentBundle.setType(packageBundleAsType(bundleType, ctx, parentBundle));

		return parentBundle;
	}

	private static void hydrateEntries(ArrayList<Observation> observations, FhirContext ctx, String sdcForm,
			ConfigValues configValues) {
		entries = new ArrayList<>();
		entries.add(patientEntry);
		entries.add(practitionerEntry);
		entries.add(createBundleEntry(getUUID(), createPractitionerRolePractitioner(ctx)));
		entries.add(createBundleEntry(getUUID(),
				createDiagnosticReport(ctx, sdcForm, patientReference, observations, configValues)));
	}

	private static BundleType packageBundleAsType(String bundleType, FhirContext ctx, Bundle parentBundle) {
		BundleType type;
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
		return type;
	}

	private static void hydrateObservations(ArrayList<Observation> observations, FhirContext ctx) {
		observations.forEach(obs -> {
			List<Coding> matchedCodes = new ArrayList<>();
			List<Identifier> practionerIdens = ((Practitioner) practitionerEntry.getResource()).getIdentifier();
			List<Reference> obsPerfomerRefs =
					IntStream.range(0, practionerIdens.size()).mapToObj(index -> {
						Identifier identifier = practionerIdens.get(index);
						Reference reference = new Reference(
								ParserHelper.createReferenceString(practitionerEntry.getResource().getResourceType(),
										identifier.getValue()));
						if (index == 0) {
							reference
									.setDisplay(
											generatePractitionerDisplay((Practitioner) practitionerEntry.getResource()))
									.setType(practitionerEntry.getResource().getResourceType().name());
						}
						return reference;
					}).collect(Collectors.toList());

			obs.setSubject(patientReference);
			obs.setPerformer(obsPerfomerRefs);
			obs.setEffective(new Period().setStart(new Date()));
			obs.getCode().getCoding().forEach(coding -> matchedCodes.addAll(getMatchingCodes("snomed", coding, ctx)));
			matchedCodes.forEach(match -> obs.getCode().addCoding(match));
		});
		observations.stream().forEach(obs -> entries.add(createBundleEntry(getUUID(), obs)));
	}

	private static List<Coding> getMatchingCodes(String system, Coding coding, FhirContext ctx) {
		List<Coding> matchingCodes = new ArrayList<>();

		// Get appropriate ConceptMap from File
		hydrateConceptMap(ctx, getFilenameForSystem(system));

		// Create new codes from matching ConceptMap info
		getFilteredCodeMap(system, coding).forEach((k, elemList) -> {
			elemList.forEach(elem -> {
				elem.getTarget()
						.forEach(tarElem -> matchingCodes.add(new Coding(k, tarElem.getCode(), tarElem.getDisplay())));
			});
		});

		return matchingCodes;
	}

	private static String getFilenameForSystem(String system) {
		String filename;
		switch (system) {
			case "snomed":
				filename = SNOMED_CONCEPTMAP_FILENAME;
				break;
			default:
				filename = SNOMED_CONCEPTMAP_FILENAME;
				break;
		}
		return filename;
	}

	// Filter for all matching coding info from the ConceptMap
	private static HashMap<String, List<SourceElementComponent>> getFilteredCodeMap(String system, Coding coding) {
		HashMap<String, List<SourceElementComponent>> groupComponents = new HashMap<>();
		conceptMap.getGroup().stream().forEach(grpVal -> {
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
		return groupComponents;
	}

	private static void hydrateConceptMap(FhirContext ctx, String filename) {
		if (conceptMap == null || conceptMap.isEmpty()) {
			InputStream inputStream = BundleHelper.class.getClassLoader().getResourceAsStream(filename);
			IBaseResource resource = ctx.newJsonParser().parseResource(inputStream);
			if (resource instanceof ConceptMap) {
				conceptMap = (ConceptMap) resource;
			}
		}
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
