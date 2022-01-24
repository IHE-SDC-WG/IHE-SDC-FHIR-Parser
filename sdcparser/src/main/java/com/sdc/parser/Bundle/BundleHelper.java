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
	
	public static Bundle createBundle(ArrayList<Observation> Observations, FhirContext ctx, String sdcForm,
			Document form, String patientUUID, String practUUID, String practRoleUUID, String docRefUUID, String messageHeaderUUID, String diagRepUUID, List ref, ConfigValues configValues) throws IOException {
		Bundle bundle = new Bundle();
		String bundleUUID = getUUID();

		bundle.setId(bundleUUID);
		bundle.setType(BundleType.MESSAGE);
		// Add message header
		BundleEntryComponent messageHeader = createBundleEntry(messageHeaderUUID, createMessageHeader(ctx));
		bundle.addEntry(messageHeader);
		// Add patient resource
		BundleEntryComponent patient = createBundleEntry(patientUUID, createPatient(configValues.getPatientConfig()));
		bundle.addEntry(patient);
		//Add Practitioner resource
		BundleEntryComponent pract = createBundleEntry(practUUID, createPractitioner(configValues.getPractitionerConfig()));
		bundle.addEntry(pract);
		//Add PractitionerRole resource
		BundleEntryComponent practRole = createBundleEntry(practRoleUUID, createPractitionerRolePractitioner(ctx));
		bundle.addEntry(practRole);
		// Add document reference resource
		//BundleEntryComponent docRef = createBundleEntry(docRefUUID, createDocReference(ctx, sdcForm, form, patientUUID));
		//bundle.addEntry(docRef);
		BundleEntryComponent diagRep = createBundleEntry(diagRepUUID, createDiagnosticReport(ctx, sdcForm, patientUUID, Observations, configValues)); 		
		bundle.addEntry(diagRep); 		
				// add observations
		for (Observation obs : Observations) {
			obs.setSubject(new Reference(patientUUID));
			//Commenting out the observation derivedFrom for DocumentReference
			//obs.addDerivedFrom().setReference(docRefUUID);
			BundleEntryComponent bec = createBundleEntry(getUUID(), obs);
			bundle.addEntry(bec);
		}
		return bundle;
	}
	
	public static BundleEntryComponent createBundleEntry(String fullUrl, Resource resource) {
		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
		bundleEntryComponent.setFullUrl(fullUrl);
		bundleEntryComponent.setResource(resource);
		return bundleEntryComponent;
	}
	
}
