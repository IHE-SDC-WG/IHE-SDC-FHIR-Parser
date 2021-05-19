package com.sdc.parser.Resource;

import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationHelper {

	public static Organization createOrganization(FhirContext ctx) {
		Organization organization = new Organization();
		organization.addIdentifier().setSystem("urn:oid:2.16.840.1.113883.4.7").setValue("IHESDCParser0");
		organization.setActive(true);
		organization.setName("IHESDCParser0");
		return organization;
	}
}
