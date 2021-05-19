package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerHelper {

	public static Practitioner createPractitioner(FhirContext ctx) {
		Practitioner pract = new Practitioner();
		pract.addName().setFamily("Karev").addGiven("Alex");
		pract.addIdentifier().setSystem("http://someIdentifier.com").setValue("pathpract1");
		pract.addIdentifier().setSystem("http://hl7.org/fhir/sid/us-npi").setValue("85940584902");
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(pract);
		return pract;
	}
}
