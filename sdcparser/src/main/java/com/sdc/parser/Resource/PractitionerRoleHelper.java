package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerRoleHelper {

	public static PractitionerRole createPractitionerRole(FhirContext ctx) {
		PractitionerRole practRole = new PractitionerRole();
		practRole.addIdentifier().setSystem("http://someIdentifier.com").setValue("pathpractRole1");
		practRole.getPractitioner().setReference("Practitioner/pathpract1").setDisplay("Dr. Alex Karev");
		practRole.getOrganization().setReference("Organization/IHESDCParser0");
		practRole.addCode().addCoding().setCode("principal-result-interpretter").setDisplay("Pathologist principally interpretting results on pathology testing").setSystem("http://www.hl7.org/fhir/us/cancer-reporting/CodeSystem/pathology-provider-types");
		//		String encoded = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(practRole);
		return practRole;
	}
}
