package com.sdc.parser;

import static com.sdc.parser.ParserHelper.getUUID;

import java.util.ArrayList;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.w3c.dom.Document;

import static com.sdc.parser.MessageHeaderHelper.createMessageHeader;
import static com.sdc.parser.PatientHelper.*;
import static com.sdc.parser.DocReferenceHelper.createDocReference;

import ca.uhn.fhir.context.FhirContext;

public class BundleHelper {
	public static Bundle createBundle(ArrayList<Observation> Observations, FhirContext ctx, String sdcForm,
			Document form, String patientUUID, String docRefUUID, String messageHeaderUUID) {
		Bundle bundle = new Bundle();
		String bundleUUID = getUUID();
		bundle.setId(bundleUUID);
		bundle.setType(BundleType.MESSAGE);
		// Add message header
		BundleEntryComponent messageHeader = createBundleEntry(messageHeaderUUID, createMessageHeader(ctx));
		bundle.addEntry(messageHeader);
		// Add patient resource
		BundleEntryComponent patient = createBundleEntry(patientUUID, createPatient(ctx));
		bundle.addEntry(patient);
		// Add document reference resource
		BundleEntryComponent docRef = createBundleEntry(docRefUUID, createDocReference(ctx, sdcForm, form, patientUUID));
		bundle.addEntry(docRef);
		// add observations
		for (Observation obs : Observations) {
			obs.setSubject(new Reference(patientUUID));
			obs.addDerivedFrom().setReference(docRefUUID);
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
