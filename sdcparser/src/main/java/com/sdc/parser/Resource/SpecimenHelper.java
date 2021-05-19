package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Specimen;

import ca.uhn.fhir.context.FhirContext;

public class SpecimenHelper {

	public static Specimen createSpecimen(FhirContext ctx) {
		Specimen specimen = new Specimen();
		specimen.addIdentifier().setSystem("http://someIdentifier.com").setValue("specimen1");
		//add a status 
		specimen.getType().addCoding().setCode("TUMOR").setDisplay("Tumor").setSystem("http://terminology.hl7.org/CodeSystem/v2-0487");
// bodysite and method are not possible in the parser since they are derived from the form. These should be coded as SNOMED
		//add collection.bodysite
		//add collection.method
		//add an accessionIdentifier 
		// having trouble setting receivedTime 
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(practRole);
		return specimen;
	}
}
