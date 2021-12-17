package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientHelper {
	public static Patient createPatient(FhirContext ctx) {
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
		patient.addIdentifier().setSystem("urn:system").setValue("JoelAlexPatient");
		patient.addName().setFamily("Rodriguez").addGiven("Jose");
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}
}
