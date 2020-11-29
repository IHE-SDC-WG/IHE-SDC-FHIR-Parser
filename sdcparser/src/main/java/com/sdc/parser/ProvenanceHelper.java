package com.sdc.parser;

import static com.sdc.parser.Constants.PROVENANCE_SYSTEM_NAME;

import java.util.Date;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.codesystems.ProvenanceAgentRole;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceHelper {
	public static Provenance createProvenance(FhirContext ctx, String bundleUUID) {
//		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date dateobj = new Date();
		Provenance provenance = new Provenance();
		provenance.setRecorded(dateobj);
		Type type = new DateTimeType();
		provenance.setOccurred(type);
		provenance.getTargetFirstRep().setReference("Bundle/" + bundleUUID);
		ProvenanceAgentComponent pac = new ProvenanceAgentComponent();
		pac.getRoleFirstRep().getCodingFirstRep().setCode(ProvenanceAgentRole.ASSEMBLER.toString())
				.setDisplay(ProvenanceAgentRole.ASSEMBLER.getDisplay()).setSystem(PROVENANCE_SYSTEM_NAME);
		provenance.addAgent(pac);
		return provenance;
	}
}
