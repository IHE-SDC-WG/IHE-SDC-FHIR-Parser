package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerHelper {

	public static Practitioner createPractitioner(FhirContext ctx) {
		Practitioner pract = new Practitioner();
        pract.getMeta().addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner");
		pract.addName().setFamily("Bit").addGiven("Rex");
		pract.addIdentifier().setSystem("http://someIdentifier.com").setValue("pathpract1");
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(pract);
		return pract;
	}
}
