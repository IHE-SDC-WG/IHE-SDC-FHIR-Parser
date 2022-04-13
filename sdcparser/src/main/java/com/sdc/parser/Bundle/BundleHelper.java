package com.sdc.parser.Bundle;

import static com.sdc.parser.ParserHelper.getUUID;
import static com.sdc.parser.Resource.DiagnosticReportHelper.createDiagnosticReport;
import static com.sdc.parser.Resource.MessageHeaderHelper.createMessageHeader;
import static com.sdc.parser.Resource.PatientHelper.createPatient;
import static com.sdc.parser.Resource.PractitionerHelper.createPractitioner;
import static com.sdc.parser.Resource.PractitionerRoleHelper.createPractitionerRolePractitioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sdc.parser.Config.ConfigValues;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.w3c.dom.Document;

import ca.uhn.fhir.context.FhirContext;

public class BundleHelper {

	public static Bundle createBundle(ArrayList<Observation> observations, FhirContext ctx, String sdcForm,
			Document form, String patientUUID, String practUUID, String practRoleUUID, String docRefUUID,
			String messageHeaderUUID, String diagRepUUID, List ref, ConfigValues configValues) throws IOException {
		Bundle parentBundle = new Bundle();
		String bundleUUID = getUUID();

		parentBundle.setId(bundleUUID);
		parentBundle.setType(BundleType.MESSAGE);

		// Add message header
		parentBundle.addEntry(createBundleEntry(messageHeaderUUID, createMessageHeader(ctx)));

		Bundle contentBundle = new Bundle();
		contentBundle.setType(BundleType.COLLECTION);

		// Add patient resource
		contentBundle.addEntry(createBundleEntry(patientUUID, createPatient(configValues.getPatientConfig())));

		// Add Practitioner resource
		contentBundle.addEntry(createBundleEntry(practUUID, createPractitioner(configValues.getPractitionerConfig())));

		// Add PractitionerRole resource
		contentBundle.addEntry(createBundleEntry(practRoleUUID, createPractitionerRolePractitioner(ctx)));

		// Add document reference resource
		contentBundle.addEntry(createBundleEntry(diagRepUUID,createDiagnosticReport(ctx, sdcForm, patientUUID, observations, configValues)));

		// add observations
		List<Observation> patientObservations = observations.stream()
				.map(obs -> obs.setSubject(new Reference(patientUUID))).toList();
		patientObservations.stream().forEach(obs -> contentBundle.addEntry(createBundleEntry(getUUID(), obs)));

		// Add contentBundle
		parentBundle.addEntry(createBundleEntry(getUUID(), contentBundle));

		return parentBundle;
	}

	public static BundleEntryComponent createBundleEntry(String fullUrl, Resource resource) {
		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
		bundleEntryComponent.setFullUrl(fullUrl);
		bundleEntryComponent.setResource(resource);
		return bundleEntryComponent;
	}

}
