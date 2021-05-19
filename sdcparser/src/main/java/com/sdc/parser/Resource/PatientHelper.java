package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientHelper {
	public static Patient createPatient(FhirContext ctx) {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("6547");
		patient.addName().setFamily("Shepard").addGiven("Meredith");
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.FEMALE);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}

}
