package com.sdc.parser.Resource;

import java.util.Map.Entry;

import com.sdc.parser.Config.PatientConfig;

import org.hl7.fhir.r4.model.Patient;

public class PatientHelper {
	public static Patient createPatient(PatientConfig patientConfig) {
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
		patient.getText().setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.GENERATED);
		patient.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">fake Observation</div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>6547</td></tr></tbody></table></div>");
		for (Entry<String, String> entry : patientConfig.getSystem().entrySet()) {
			patient.addIdentifier().setSystem(entry.getKey()).setValue(entry.getValue());
		}
		
		patient.addName().setFamily(patientConfig.getLastName()).addGiven(patientConfig.getFirstName());
		patient.setGender(org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.FEMALE);
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(patient);
		return patient;
	}
}
