package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Specimen;

import ca.uhn.fhir.context.FhirContext;

public class SpecimenHelper {

	public static Specimen createSpecimen(FhirContext ctx) {
		Specimen specimen = new Specimen();
		specimen.addIdentifier().setSystem("http://someIdentifier.com").setValue("specimen1");
		//add a status 
		specimen.getType().addCoding().setCode("TUMOR").setDisplay("Tumor").setSystem("http://terminology.hl7.org/ValueSet/v2-0487");
		//add collection.bodysite
		//addcollection.method
		//add an accessionIdentifier 
		// having trouble setting receivedTime 
//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(practRole);
		return specimen;
	}
}
