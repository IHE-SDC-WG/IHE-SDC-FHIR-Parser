package com.sdc.parser.Resource;

import java.util.Map.Entry;

import com.sdc.parser.Config.PatientConfig;

import org.hl7.fhir.r4.model.Patient;

public class PatientHelper {
	public static Patient createPatient(PatientConfig patientConfig) {
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");

		for (Entry<String, String> entry : patientConfig.getSystem().entrySet()) {
			patient.addIdentifier().setSystem(entry.getKey()).setValue(entry.getValue());
		}
//		patient.setId(cancerPathPatient);
		patient.addName().setFamily(patientConfig.getLastName()).addGiven(patientConfig.getFirstName());
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}
}
