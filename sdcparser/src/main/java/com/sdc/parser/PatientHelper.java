package com.sdc.parser;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PatientHelper {
	public static Patient createPatient(FhirContext ctx) {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("JoelAlexPatient");
		patient.addName().setFamily("Rodriguez").addGiven("Jose");
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}

	public static Practitioner createPractitioner(FhirContext ctx) {
		Practitioner pract = new Practitioner();
		pract.addName().setFamily("Bit").addGiven("Rex");
		pract.addIdentifier().setSystem("http://someIdentifier.com").setValue("pathpract1");
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(pract);
		return pract;
	}
}
