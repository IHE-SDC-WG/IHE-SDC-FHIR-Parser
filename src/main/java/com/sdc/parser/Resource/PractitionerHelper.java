package com.sdc.parser.Resource;

import java.util.Map.Entry;

import com.sdc.parser.Config.PractitionerConfig;

import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerHelper {

	public static Practitioner createPractitioner(PractitionerConfig practitionerConfig) {
		Practitioner pract = new Practitioner();
        pract.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner");
		pract.getText().setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.GENERATED);
		pract.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">fake Observation</div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>6547</td></tr></tbody></table></div>");
		pract.addName().setFamily(practitionerConfig.getLastName()).addGiven(practitionerConfig.getFirstName());

		 for (Entry<String, String> entry : practitionerConfig.getSystem().entrySet()) {
			pract.addIdentifier().setSystem(entry.getKey()).setValue(entry.getValue());
		}

		return pract;
	}

	public static String generatePractitionerDisplay(Practitioner practitioner) {
		String first = String.valueOf(practitioner.getNameFirstRep().getGiven().get(0).asStringValue().charAt(0)).toUpperCase() + ".";
		String last = practitioner.getNameFirstRep().getFamily();
		return first + " " + last;
	}
}
