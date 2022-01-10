package com.sdc.parser.Resource;

import java.util.Map.Entry;

import com.sdc.parser.Config.PractitionerConfig;

import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerHelper {

	public static Practitioner createPractitioner(PractitionerConfig practitionerConfig) {
		Practitioner pract = new Practitioner();
        pract.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner");
		pract.addName().setFamily(practitionerConfig.getLastName()).addGiven(practitionerConfig.getFirstName());

		 for (Entry<String, String> entry : practitionerConfig.getSystem().entrySet()) {
			pract.addIdentifier().setSystem(entry.getKey()).setValue(entry.getValue());
		}

		return pract;
	}
}
